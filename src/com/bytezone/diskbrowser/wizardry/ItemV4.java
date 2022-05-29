package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class ItemV4 extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  String name;
  String nameGeneric;

  // ---------------------------------------------------------------------------------//
  ItemV4 (String[] names, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (names[1], buffer);

    name = names[1];
    nameGeneric = names[0];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return HexFormatter.format (buffer, 1, buffer[0] & 0xFF);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }
}
