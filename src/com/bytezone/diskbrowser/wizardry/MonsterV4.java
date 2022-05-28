package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class MonsterV4 extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  MonsterV4 (String[] names, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (names[2], buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return HexFormatter.format (buffer, 1, buffer[0] & 0xFF);
  }
}
