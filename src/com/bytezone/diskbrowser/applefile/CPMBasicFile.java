package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.getShort;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class CPMBasicFile extends TextFile
// -----------------------------------------------------------------------------------//
{
  String[] tokens = { //
      "", "END", "FOR", "NEXT", "DATA", "INPUT", "DIM", "READ",   // 0x80
      "", "GOTO", "RUN", "IF", "", "GOSUB", "RETURN", "REM",      // 0x88
      "POS", "PRINT", "CLEAR", "", "", "ON", "DEF", "POKE",       // 0x90
      "", "", "", "", "", "", "ELSE", "",                         // 0x98
      "", "", "", "", "", "", "", "",                             // 0xA0
      "", "", "DEFINT", "", "", "LINE", "", "",                   // 0xA8
      "", "", "", "COMMON", "CHAIN", "", "", "SYSTEM",            // 0xB0
      "OPEN", "FIELD", "GET", "", "CLOSE", "", "", "",            // 0xB8
      "", "KILL", "LSET", "", "", "RESET", "TEXT", "HOME",        // 0xC0
      "VTAB", "HTAB", "INVERSE", "NORMAL", "", "", "", "",        // 0xC8
      "", "", "", "", "", "", "", "",                             // 0xD0
      "", "", "", "", "", "TO", "THEN", "TAB(",                   // 0xD8
      "STEP", "USR", "FN", "SPC(", "", "", "", "STRING$",         // 0xE0
      "USING", "INSTR", "", "VARPTR", "", "", "INKEY$", ">",      // 0xE8
      "=", "<", "+", "-", "*", "/", "", "AND",                    // 0xF0
      "OR", "", "", "", "MOD", "/", "", "",                       // 0xF8
  };

  // ---------------------------------------------------------------------------------//
  public CPMBasicFile (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    if (textPreferences.showHeader)
      text.append ("Name : " + name + "\n\n");

    int ptr = 5;
    while (buffer[ptr] != 0)
      ptr++;

    int loadAddress = getShort (buffer, 1) - ptr - 1;

    ptr = 1;
    while (ptr < buffer.length)
    {
      int nextAddress = getShort (buffer, ptr);

      if (nextAddress == 0)
        break;

      int lineNumber = getShort (buffer, ptr + 2);
//      System.out.println (lineNumber);

      text.append (String.format (" %d ", lineNumber));
      ptr += 4;

      int end = nextAddress - loadAddress - 1;
      while (ptr < end)
      {
        int val = buffer[ptr++] & 0xFF;

        if (val >= 0x20 && val <= 0x7E)              // printable
        {
          text.append (String.format ("%s", (char) val));
          continue;
        }

        if (val >= 0x11 && val <= 0x1A)
        {
          text.append (val - 0x11);
          continue;
        }

        switch (val)
        {
          case 0x07:
            text.append ("<BELL>");
            break;

          case 0x09:
            text.append ("         ");
            break;

          case 0x0A:
            text.append ("\n");
            break;

          case 0x0C:
            int b1 = buffer[ptr++] & 0xFF;
            int b2 = buffer[ptr++] & 0xFF;
            text.append ("&H" + String.format ("%X", b2 * 256 + b1));
            break;

//          case 0x0D:
//            System.out.println ("found 0x0D");
//            break;

          case 0x0E:                                // same as 0x1C ??
            b1 = buffer[ptr++] & 0xFF;
            b2 = buffer[ptr++] & 0xFF;
            text.append (b2 * 256 + b1);
            break;

          case 0x0F:
            int nextVal = buffer[ptr++] & 0xFF;
            text.append (nextVal);
            break;

          case 0x1C:                                // same as 0x0E ??
            b1 = buffer[ptr++] & 0xFF;
            b2 = buffer[ptr++] & 0xFF;
            text.append (b2 * 256 + b1);
            break;

          case 0x1D:
            text.append ("<" + HexFormatter.getHexString (buffer, ptr, 4, true) + ">");
            ptr += 4;
            break;

          case 0x1F:
            text.append ("<" + HexFormatter.getHexString (buffer, ptr, 8, true) + ">");
            ptr += 8;
            break;

          case 0xFF:
            int next = buffer[ptr++] & 0xFF;
            String token = switch (next)
            {
              case 0x81 -> "LEFT$";
              case 0x82 -> "RIGHT$";
              case 0x83 -> "MID$";
              case 0x8F -> "FRE";
              case 0x91 -> "LEN";
              case 0x94 -> "ASC";
              case 0x95 -> "CHR$";
              case 0x96 -> "PEEK";
              default -> String.format ("<%02X>", next);
            };
            text.append (token);
            break;

          default:
            text.append (getToken (val));
        }
      }

      ptr = nextAddress - loadAddress;
      text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String getToken (int val)
  // ---------------------------------------------------------------------------------//
  {
    if (val < 0x80)
      return String.format ("<****%02X*****>", val);

    String token = tokens[val - 0x80];
    if (token.length () == 0)
    {
      token = String.format ("<%02X>", val);
//      System.out.println (token);
    }

    return token;
  }
}
