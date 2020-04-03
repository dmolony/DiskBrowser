package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class FinderData extends AbstractFile
// -----------------------------------------------------------------------------------//
{

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

    text.append (HexFormatter.format (buffer, 0, 41));
    text.append ("\n\n");

    int ptr = 41;
    while (ptr < buffer.length - 1)
    {
      String line = HexFormatter.getHexString (buffer, ptr, 9);
      text.append (line + "  ");

      ptr += 9;
      String name = HexFormatter.getPascalString (buffer, ptr);
      text.append (name + "\n");

      ptr += name.length () + 1;
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}
