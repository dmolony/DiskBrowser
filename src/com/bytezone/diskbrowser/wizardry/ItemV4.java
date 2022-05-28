package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class ItemV4 extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  ItemV4 (String[] names, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (names[1], buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return HexFormatter.format (buffer, 1, buffer[0] & 0xFF);
  }
}
