package com.bytezone.diskbrowser.disk;

import com.bytezone.diskbrowser.disk.MC3470.DiskSector;

public abstract class DiskReader
{
  static final int BLOCK_SIZE = 256;
  static final byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };

  final int sectorsPerTrack;
  boolean debug = false;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  DiskReader (int sectorsPerTrack)
  {
    this.sectorsPerTrack = sectorsPerTrack;
  }

  // ---------------------------------------------------------------------------------//
  // abstract functions
  // ---------------------------------------------------------------------------------//

  abstract byte[] decodeSector (byte[] buffer, int ptr);

  abstract byte[] encodeSector (byte[] buffer);

  abstract void storeBuffer (DiskSector diskSector, byte[] diskBuffer);

  abstract int expectedDataSize ();
}
