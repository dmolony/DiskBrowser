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
        if ((val & 0x80) == 0 && val >= 32)
          text.append (String.format ("%s", (char) val));     // printable
        else
          text.append (String.format ("<%02X>", val));        // token?
      }

      ptr = nextAddress - loadAddress;
      text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
