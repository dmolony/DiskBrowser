package com.bytezone.diskbrowser.prodos;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public abstract class DirectoryHeader extends CatalogEntry implements ProdosConstants
// -----------------------------------------------------------------------------------//
{
  final int entryLength;
  final int entriesPerBlock;
  final int fileCount;

  // ---------------------------------------------------------------------------------//
  DirectoryHeader (ProdosDisk parentDisk, byte[] entryBuffer, int blockNo, int entryNo)
  // ---------------------------------------------------------------------------------//
  {
    super (parentDisk, entryBuffer, blockNo, entryNo);

    entryLength = entryBuffer[31] & 0xFF;
    entriesPerBlock = entryBuffer[32] & 0xFF;
    fileCount = Utility.intValue (entryBuffer[33], entryBuffer[34]);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%s  %04X", super.getText (), fileCount);
  }

  // ---------------------------------------------------------------------------------//
  public void listFileEntries (StringBuilder text)
  // ---------------------------------------------------------------------------------//
  {
    int blockNo = this.blockNo;

    do
    {
      byte[] buffer = disk.readBlock (blockNo);
      int ptr = 4;
      int entryNo = 1;
      for (int i = 0; i < 13; i++)
      {
        int nameLength = buffer[ptr] & 0x0F;
        int storageType = (buffer[ptr] & 0xF0) >>> 4;
        if (nameLength > 0 && storageType < 0x0E)
        {
          String name = new String (buffer, ptr + 1, nameLength);
          int blocksUsed = Utility.intValue (buffer[ptr + 0x13], buffer[ptr + 0x14]);
          int fileType = buffer[ptr + 0x10] & 0xFF;
          int keyPointer = Utility.intValue (buffer[ptr + 0x11], buffer[ptr + 0x12]);
          int headerPointer = Utility.intValue (buffer[ptr + 0x25], buffer[ptr + 0x26]);
          text.append (String.format ("%04X:%02X  %-15s  %s  %04X  %s  %04X  %04X%n",
              blockNo, entryNo, name, storageTypes[storageType], blocksUsed,
              fileTypes[fileType], keyPointer, headerPointer));
        }
        ptr += 0x27;
        ++entryNo;
      }

      blockNo = Utility.intValue (buffer[2], buffer[3]);
    } while (blockNo != 0);
  }
}