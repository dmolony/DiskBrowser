package com.bytezone.diskbrowser.pascal;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.utilities.Utility;

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

    totalBlocks = Utility.getShort (buffer, 14);              // 280
    totalFiles = Utility.getShort (buffer, 16);
    localDate = Utility.getPascalLocalDate (buffer, 20);      // 2 bytes
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AbstractFile getDataSource ()
  // ---------------------------------------------------------------------------------//
  {
    if (file != null)
      return file;

    byte[] buffer = parent.getDisk ().readBlocks (blocks);
    file = new DefaultAppleFile (name, buffer);

    return file;
  }
}