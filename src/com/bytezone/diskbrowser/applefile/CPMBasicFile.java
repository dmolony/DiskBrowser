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

    int offset = getShort (buffer, 1) - ptr;

    ptr = 1;
    while (ptr < buffer.length)
    {
      int val1 = getShort (buffer, ptr);

      if (val1 == 0)
        break;

      int val2 = getShort (buffer, ptr + 2);

      text.append (String.format ("%7d  ", val2));
      ptr += 4;

      while (buffer[ptr] != 0)
      {
        int val = buffer[ptr++] & 0xFF;
        if ((val & 0x80) == 0 && val >= 32)
          text.append (String.format ("%s", (char) val));
        else
          text.append (String.format ("<%02X>", val));
      }

      ptr = val1 - offset + 1;
      text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
