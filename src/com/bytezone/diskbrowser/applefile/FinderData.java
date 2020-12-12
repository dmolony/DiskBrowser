package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class FinderData extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  int version = buffer[0];

  // ---------------------------------------------------------------------------------//
  public FinderData (String name, byte[] buffer)
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

    text.append ("Name : " + name + "\n\n");

    text.append ("\n\n");

    if (version == 1)
    {
      int ptr = 16;
      text.append (HexFormatter.getHexString (buffer, 0, ptr));
      text.append ("\n\n");
      while (buffer[ptr] != 0)
      {
        String line = HexFormatter.getHexString (buffer, ptr, 6);
        text.append (line + "  ");

        String name = HexFormatter.getPascalString (buffer, ptr + 6);
        text.append (name + "\n");

        ptr += 22;
      }
    }
    else if (version == 2)
    {
      int totFiles = buffer[34];
      int ptr = 42;
      text.append (HexFormatter.format (buffer, 0, ptr));
      text.append ("\n\n");

      for (int i = 0; i < totFiles; i++)
      {
        String line = HexFormatter.getHexString (buffer, ptr, 8);
        text.append (line + "  ");

        ptr += 8;
        String name = HexFormatter.getPascalString (buffer, ptr);
        text.append (String.format ("%-20s ", name));

        ptr += name.length () + 1;
        text.append (String.format ("%02X%n", buffer[ptr++]));
      }
    }
    else
      text.append (String.format ("Unknown finder data version: %d%n", version));

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
