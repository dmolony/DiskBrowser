package com.bytezone.diskbrowser.pascal;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class VolumeEntry extends CatalogEntry
// -----------------------------------------------------------------------------------//
{
  final int totalFiles;
  final int totalBlocks;

  // ---------------------------------------------------------------------------------//
  VolumeEntry (PascalDisk parent, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (parent, buffer);

    totalBlocks = HexFormatter.intValue (buffer[14], buffer[15]);         // 280
    totalFiles = HexFormatter.intValue (buffer[16], buffer[17]);
    date = HexFormatter.getPascalDate (buffer, 20);                       // 2 bytes
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AbstractFile getDataSource ()
  // ---------------------------------------------------------------------------------//
  {
    if (file != null)
      return file;

    byte[] buffer = parent.getDisk ().readSectors (blocks);
    file = new DefaultAppleFile (name, buffer);

    return file;
  }
}