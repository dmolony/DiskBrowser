package com.bytezone.diskbrowser.prodos;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
abstract class DirectoryHeader extends CatalogEntry
// -----------------------------------------------------------------------------------//
{
  final int entryLength;
  final int entriesPerBlock;
  final int fileCount;

  // ---------------------------------------------------------------------------------//
  DirectoryHeader (ProdosDisk parentDisk, byte[] entryBuffer)
  // ---------------------------------------------------------------------------------//
  {
    super (parentDisk, entryBuffer);

    entryLength = entryBuffer[31] & 0xFF;
    entriesPerBlock = entryBuffer[32] & 0xFF;
    fileCount = Utility.intValue (entryBuffer[33], entryBuffer[34]);
  }
}