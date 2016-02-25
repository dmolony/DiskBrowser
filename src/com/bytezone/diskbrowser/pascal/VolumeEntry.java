package com.bytezone.diskbrowser.pascal;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class VolumeEntry extends CatalogEntry
{
  int totalFiles;
  int totalBlocks;

  public VolumeEntry (PascalDisk parent, byte[] buffer)
  {
    super (parent, buffer);

    totalBlocks = HexFormatter.intValue (buffer[14], buffer[15]);
    totalFiles = HexFormatter.intValue (buffer[16], buffer[17]);
    firstBlock = HexFormatter.intValue (buffer[18], buffer[19]);
    date = HexFormatter.getPascalDate (buffer, 20);

    //      for (int i = firstBlock; i < lastBlock; i++)
    //        sectorType[i] = catalogSector;
  }

  @Override
  public AbstractFile getDataSource ()
  {
    if (file != null)
      return file;

    byte[] buffer = parent.getDisk ().readSectors (blocks);
    file = new DefaultAppleFile (name, buffer);

    return file;
  }
}