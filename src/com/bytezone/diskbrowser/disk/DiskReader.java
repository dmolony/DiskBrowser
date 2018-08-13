package com.bytezone.diskbrowser.disk;

public abstract class DiskReader
{
  static final int BLOCK_SIZE = 256;
  static final byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };

  final int sectorsPerTrack;

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

  abstract byte[] decodeSector (byte[] buffer) throws DiskNibbleException;

  abstract byte[] encodeSector (byte[] buffer);

  abstract void storeBuffer (RawDiskSector diskSector, byte[] diskBuffer);

  abstract int expectedDataSize ();
}
