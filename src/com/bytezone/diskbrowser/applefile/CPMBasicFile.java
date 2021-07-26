package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.getShort;

// -----------------------------------------------------------------------------------//
public class CPMBasicFile extends TextFile
// -----------------------------------------------------------------------------------//
{

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

      text.append (String.format ("%7d  ", lineNumber));
      ptr += 4;

      while (buffer[ptr] != 0)
      {
        int val = buffer[ptr++] & 0xFF;

        if (val == 0x0E)
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

        if (val >= 0x11 && val <= 0x1A)
        {
          text.append (val - 0x11);
          continue;
        }

        if (val == 0xFF)
        {
          int next = buffer[ptr++] & 0xFF;
          String token = switch (next)
          {
            case 0x94 -> "ASC";
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
    switch (val)
    {
      case 0x81:
        return "END";

      case 0x82:
        return "FOR";

      case 0x83:
        return "NEXT";

      case 0x84:
        return "DATA";

      case 0x85:
        return "INPUT";

      case 0x8B:
        return "IF";

      case 0x89:
        return "GOTO";

      case 0x8D:
        return "GOSUB";

      case 0x8E:
        return "RETURN";

      case 0x8F:
        return "REM";

      case 0x90:
        return "POS";

      case 0x91:
        return "PRINT";

      case 0x95:
        return "ON";

      case 0x97:
        return "POKE";

      case 0x9E:
        return "ELSE";

      case 0xB3:
        return "DIM ";

      case 0xC5:
        return "RESET";

      case 0xC6:
        return "TEXT";

      case 0xC7:
        return "HOME";

      case 0xC8:
        return "VTAB";

      case 0xC9:
        return "HTAB";

      case 0xDD:
        return "TO";

      case 0xDE:
        return "THEN";

      case 0xDF:
        return "TAB(";

      case 0xE0:
        return "STEP";

      case 0xE1:
        return "USR";

      case 0xE7:
        return "STRING$";

      case 0xEF:
        return ">";

      case 0xF0:
        return "=";

      case 0xF1:
        return "<";

      case 0xF2:
        return "+";

      case 0xF3:
        return "-";

      case 0xF4:
        return "*";

      case 0xF7:
        return "AND";

      case 0xF8:
        return "OR";

      case 0xFC:
        return "MOD";

      default:
        return String.format ("<%02X>", val);
    }
  }
}
