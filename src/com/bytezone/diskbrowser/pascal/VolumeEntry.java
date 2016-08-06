package com.bytezone.diskbrowser.pascal;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class VolumeEntry extends CatalogEntry
{
  final int totalFiles;
  final int totalBlocks;
  final int block1;                   // first block on the disk (usually 0)
  final int lastDirectoryBlock;       // (plus 1) (usually 6)
  final int recordType;               // 0 = directory
  final int nameLength;
  final String name;

  public VolumeEntry (PascalDisk parent, byte[] buffer)
  {
    super (parent, buffer);

    block1 = HexFormatter.intValue (buffer[0], buffer[1]);                // 0
    lastDirectoryBlock = HexFormatter.intValue (buffer[2], buffer[3]);    // 6
    recordType = HexFormatter.intValue (buffer[4], buffer[5]);            // 0
    nameLength = buffer[6] & 0xFF;
    name = HexFormatter.getPascalString (buffer, 6);                      // 06-0D
    totalBlocks = HexFormatter.intValue (buffer[14], buffer[15]);         // 280
    totalFiles = HexFormatter.intValue (buffer[16], buffer[17]);
    firstBlock = HexFormatter.intValue (buffer[18], buffer[19]);          // 0
    date = HexFormatter.getPascalDate (buffer, 20);                       // 2 bytes
    // bytes 0x16 - 0x19 are unused

    if (false)
    {
      System.out.printf ("Total files ..... %d%n", totalFiles);
      System.out.printf ("Total blocks .... %d%n", totalBlocks);
      System.out.printf ("Block1 .......... %d%n", block1);
      System.out.printf ("Last block ...... %d%n", lastDirectoryBlock);
      System.out.printf ("Record type ..... %d%n", recordType);
      System.out.printf ("Name length ..... %d%n", nameLength);
      System.out.printf ("Name ............ %s%n", name);
    }
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