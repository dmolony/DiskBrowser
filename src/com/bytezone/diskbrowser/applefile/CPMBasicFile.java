package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.getShort;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class CPMBasicFile extends BasicProgram
// -----------------------------------------------------------------------------------//
{
  String[] tokens = { //
      "", "END", "FOR", "NEXT", "DATA", "INPUT", "DIM", "READ",           // 0x80
      "LET", "GOTO", "RUN", "IF", "RESTORE", "GOSUB", "RETURN", "REM",    // 0x88
      "STOP", "PRINT", "CLEAR", "LIST", "NEW", "ON", "DEF", "POKE",       // 0x90
      "", "", "", "LPRINT", "LLIST", "WIDTH", "ELSE", "",                 // 0x98
      "", "SWAP", "ERASE", "", "ERROR", "RESUME", "DELETE", "",           // 0xA0
      "RENUM", "DEFSTR", "DEFINT", "", "DEFDBL", "LINE", "", "WHILE",     // 0xA8
      "WEND", "CALL", "WRITE", "COMMON", "CHAIN",                         // 0xB0
      "OPTION", "RANDOMIZE", "SYSTEM",                                    // 0xB5
      "OPEN", "FIELD", "GET", "PUT", "CLOSE", "LOAD", "MERGE", "",        // 0xB8
      "NAME", "KILL", "LSET", "RSET", "SAVE", "RESET", "TEXT", "HOME",    // 0xC0
      "VTAB", "HTAB", "INVERSE", "NORMAL", "", "", "", "",                // 0xC8
      "", "", "", "", "", "WAIT", "", "",                                 // 0xD0
      "", "", "", "", "", "TO", "THEN", "TAB(",                           // 0xD8
      "STEP", "USR", "FN", "SPC(", "", "ERL", "ERR", "STRING$",           // 0xE0
      "USING", "INSTR", "'", "VARPTR", "", "", "INKEY$", ">",             // 0xE8
      "=", "<", "+", "-", "*", "/", "", "AND",                            // 0xF0
      "OR", "", "", "", "MOD", "/", "", "",                               // 0xF8
  };

  String[] functions = { //
      "", "LEFT$", "RIGHT$", "MID$", "SGN", "INT", "ABS", "SQR",      // 0x80
      "RND", "SIN", "LOG", "EXP", "COS", "TAN", "ATN", "FRE",         // 0x88
      "POS", "LEN", "STR$", "VAL", "ASC", "CHR$", "PEEK", "SPACE$",   // 0x90
      "OCT$", "HEX$", "LPOS", "CINT", "CSNG", "CDBL", "FIX", "",      // 0x98
      "", "", "", "", "", "", "", "",                                 // 0xA0
      "", "", "CVI", "CVS", "CVD", "", "EOF", "LOC",                  // 0xA8
      "", "MKI$", "MKS$", "MKD$",                                     // 0xB0
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

    if (basicPreferences.showHeader)
      text.append ("Name : " + name + "\n\n");

    int ptr = 5;
    while (buffer[ptr] != 0)
      ptr++;

    int loadAddress = getShort (buffer, 1) - ptr - 1;
    if (!validate (buffer, loadAddress))
      System.out.println ("Invalid load address");

    if (showDebugText)
      return debugText (loadAddress);

    ptr = 1;
    while (ptr < buffer.length)
    {
      int nextAddress = getShort (buffer, ptr);

      if (nextAddress == 0)
        break;

      int lineNumber = getShort (buffer, ptr + 2);

      text.append (String.format (" %d ", lineNumber));
      ptr += 4;

      int end = nextAddress - loadAddress - 1;
      while (ptr < end)
      {
        int val = buffer[ptr++] & 0xFF;

        if (val >= 0x80)
        {
          if (val == 0xFF)
          {
            val = buffer[ptr++] & 0xFF;
            String token = functions[val & 0x7F];
            if (token.length () == 0)
              token = String.format ("<FF %02X>", val);
            text.append (token);
          }
          else
          {
            String token = tokens[val & 0x7F];
            if (token.length () == 0)
              token = String.format ("<%02X>", val);
            text.append (token);
          }
          continue;
        }

        if (val >= 0x20 && val <= 0x7E)              // printable
        {
          // check for stupid apostrophe comment
          if (val == 0x3A && ptr + 1 < buffer.length && buffer[ptr] == (byte) 0x8F
              && buffer[ptr + 1] == (byte) 0xEA)
          {
            text.append ("'");
            ptr += 2;
          }
          else if (val == 0x3A && ptr < buffer.length && buffer[ptr] == (byte) 0x9E)
          {
            // ignore colon before ELSE
          }
          else
            text.append (String.format ("%s", (char) val));
          continue;
        }

        if (val >= 0x11 && val <= 0x1A)               // inline numbers
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
            text.append ("        ");
            break;

          case 0x0A:
            text.append ("\n ");
            break;

          case 0x0C:
            int b1 = buffer[ptr++] & 0xFF;
            int b2 = buffer[ptr++] & 0xFF;
            text.append ("&H" + String.format ("%X", b2 * 256 + b1));
            break;

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

          default:
            text.append (String.format ("<%02X>", val));
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
  private String debugText (int loadAddress)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int ptr = 1;
    int lastPtr;

    while (ptr < buffer.length)
    {
      int nextAddress = getShort (buffer, ptr);
      if (nextAddress == 0)
        break;

      int lineNumber = getShort (buffer, ptr + 2);
      text.append (String.format (" %d  ", lineNumber));

      lastPtr = ptr;
      ptr = nextAddress - loadAddress;

      text.append (HexFormatter.getHexString (buffer, lastPtr + 4, ptr - lastPtr));
      text.append ("\n");
      if (ptr < 0 || ptr >= buffer.length)
        break;
      if (buffer[ptr - 1] != 0)             // end of previous line
        break;
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private boolean validate (byte[] buffer, int loadAddress)
  // ---------------------------------------------------------------------------------//
  {
//    System.out.printf ("Load Address: %04X%n", loadAddress);
    int ptr = 1;

    while (ptr < buffer.length)
    {
      int nextAddress = getShort (buffer, ptr);
//      System.out.printf ("%04X%n", nextAddress);
      if (nextAddress == 0)
        return true;
      ptr = nextAddress - loadAddress;
      if (ptr < 0 || ptr >= buffer.length)
        return false;
      if (buffer[ptr - 1] != 0)             // end of previous line
        return false;
    }

    return false;
  }
}
