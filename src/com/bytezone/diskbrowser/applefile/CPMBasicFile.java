package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.getShort;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class CPMBasicFile extends TextFile
// -----------------------------------------------------------------------------------//
{
  String[] tokens = { //
      "", "END", "FOR", "NEXT", "DATA", "INPUT", "DIM", "READ",   // 0x80
      "", "GOTO", "", "IF", "", "GOSUB", "RETURN", "REM",         // 0x88
      "POS", "PRINT", "", "", "", "ON", "DEF", "POKE",            // 0x90
      "", "", "", "", "", "", "ELSE", "",                         // 0x98
      "", "", "", "", "", "", "", "",                             // 0xA0
      "", "", "DEFINT", "", "", "", "", "",                       // 0xA8
      "", "", "", "DIM", "", "", "", "",                          // 0xB0
      "", "", "", "", "CLOSE", "", "", "",                        // 0xB8
      "", "", "LSET", "", "", "RESET", "TEXT", "HOME",            // 0xC0
      "VTAB", "HTAB", "", "", "", "", "", "",                     // 0xC8
      "", "", "", "", "", "", "", "",                             // 0xD0
      "", "", "", "", "", "TO", "THEN", "TAB(",                   // 0xD8
      "STEP", "USR", "FN", "", "", "", "", "STRING$",             // 0xE0
      "", "", "", "", "", "", "", ">",                            // 0xE8
      "=", "<", "+", "-", "*", "", "", "AND",                     // 0xF0
      "OR", "", "", "", "MOD", "", "", "",                        // 0xF8
  };

  // ---------------------------------------------------------------------------------//
  public CPMBasicFile (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    if (false)
      for (int i = 0; i < tokens.length; i++)
      {
        String t1 = tokens[i];
        String t2 = getToken (i + 0x80);

        String flag = t1.length () == 0 || t1.equals (t2) ? "" : "***************";
        System.out.printf ("%02X  %-8s %-8s %s%n", i + 0x80, t1, t2, flag);
      }
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

      text.append (String.format (" %d ", lineNumber));
      ptr += 4;

      while (buffer[ptr] != 0)
      {
        int val = buffer[ptr++] & 0xFF;

        if (val == 0x07)
        {
          text.append ("<BELL>");
          continue;
        }

        if (val == 0x09)
        {
          text.append ("         ");
          continue;
        }

        if (val == 0x0A)
        {
          text.append ("\n");
          continue;
        }

        if (val == 0x0C)
        {
          int b1 = buffer[ptr++] & 0xFF;
          int b2 = buffer[ptr++] & 0xFF;
          text.append ("&H" + String.format ("%X", b2 * 256 + b1));
          continue;
        }

        if (val == 0x0D)
        {
          System.out.println ("found 0x0D");
        }

        if (val == 0x0E)    // same as 0x1C ??
        {
          int next1 = buffer[ptr++] & 0xFF;
          int next2 = buffer[ptr++] & 0xFF;
          text.append (next2 * 256 + next1);
          continue;
        }

        if (val == 0x0F)
        {
          int nextVal = buffer[ptr++] & 0xFF;
          text.append (nextVal);
          continue;
        }

        if (val >= 0x11 && val <= 0x1A)
        {
          text.append (val - 0x11);
          continue;
        }

        if (val == 0x1C)    // same as 0x0E ??
        {
          int b1 = buffer[ptr++] & 0xFF;
          int b2 = buffer[ptr++] & 0xFF;
          text.append (b2 * 256 + b1);
          continue;
        }

        if (val == 0x1D)
        {
          text.append ("<" + HexFormatter.getHexString (buffer, ptr, 4, true) + ">");
          ptr += 4;
          continue;
        }

        if (val == 0x1F)
        {
          text.append ("<" + HexFormatter.getHexString (buffer, ptr, 8, true) + ">");
          ptr += 8;
          continue;
        }

        if (val == 0xFF)
        {
          int next = buffer[ptr++] & 0xFF;
          String token = switch (next)
          {
            case 0x81 -> "LEFT$";
            case 0x82 -> "RIGHT$";
            case 0x83 -> "MID$";
            case 0x94 -> "ASC";
            case 0x95 -> "CHR$";
            case 0x96 -> "PEEK";
            default -> String.format ("<%02X>", next);
          };
          text.append (token);
          continue;
        }

        if (val >= 0x20 && val <= 0x7E)
          text.append (String.format ("%s", (char) val));     // printable
        else
          text.append (getToken (val));
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
    {
      return String.format ("<****%02X*****>", val);
    }

    String token = tokens[val - 128];
    if (token.length () == 0)
      token = String.format ("<%02X>", val);

    return token;

//    switch (val)
//    {
//      case 0x81:
//        return "END";
//
//      case 0x82:
//        return "FOR";
//
//      case 0x83:
//        return "NEXT";
//
//      case 0x84:
//        return "DATA";
//
//      case 0x85:
//        return "INPUT";
//
//      case 0x86:
//        return "DIM";
//
//      case 0x87:
//        return "READ";
//
//      case 0x89:
//        return "GOTO";
//
//      case 0x8B:
//        return "IF";
//
//      case 0x8D:
//        return "GOSUB";
//
//      case 0x8E:
//        return "RETURN";
//
//      case 0x8F:
//        return "REM";
//
//      case 0x90:
//        return "POS";
//
//      case 0x91:
//        return "PRINT";
//
//      case 0x95:
//        return "ON";
//
//      case 0x96:
//        return "DEF";
//
//      case 0x97:
//        return "POKE";
//
//      case 0x9E:
//        return "ELSE";
//
//      case 0xAA:
//        return "DEFINT";
//
//      case 0xB3:
//        return "DIM";
//
//      case 0xC2:
//        return "LSET";
//
//      case 0xC5:
//        return "RESET";
//
//      case 0xC6:
//        return "TEXT";
//
//      case 0xC7:
//        return "HOME";
//
//      case 0xC8:
//        return "VTAB";
//
//      case 0xC9:
//        return "HTAB";
//
//      case 0xDD:
//        return "TO";
//
//      case 0xDE:
//        return "THEN";
//
//      case 0xDF:
//        return "TAB(";
//
//      case 0xE0:
//        return "STEP";
//
//      case 0xE1:
//        return "USR";
//
//      case 0xE2:
//        return "FN";
//
//      case 0xE7:
//        return "STRING$";
//
//      case 0xEF:
//        return ">";
//
//      case 0xF0:
//        return "=";
//
//      case 0xF1:
//        return "<";
//
//      case 0xF2:
//        return "+";
//
//      case 0xF3:
//        return "-";
//
//      case 0xF4:
//        return "*";
//
//      case 0xF7:
//        return "AND";
//
//      case 0xF8:
//        return "OR";
//
//      case 0xFC:
//        return "MOD";
//
//      default:
//        return String.format ("<%02X>", val);
//    }
  }
}
