package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class FinderData extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  int totFiles;

  // ---------------------------------------------------------------------------------//
  public FinderData (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    int xx = buffer[40] & 0xFF;
    if (xx == 0x2A)
      totFiles = buffer[34] & 0xFF;     // not always
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Name : " + name + "\n\n");

    if (buffer.length < 50)
    {
      text.append (HexFormatter.format (buffer));
      return text.toString ();
    }

    text.append (HexFormatter.format (buffer, 0, 42));
    text.append ("\n\n");

    int ptr = 42;
    for (int i = 0; i < totFiles; i++)
    {
      String line = HexFormatter.getHexString (buffer, ptr, 8);
      text.append (line + "  ");

      ptr += 8;
      String name = HexFormatter.getPascalString (buffer, ptr);
      //      text.append (name + "\n");
      text.append (String.format ("%-20s ", name));

      ptr += name.length () + 1;
      text.append (String.format ("%02X%n", buffer[ptr++]));
    }

    //    if (text.length () > 0)
    //      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
