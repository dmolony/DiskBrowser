package com.bytezone.diskbrowser.nib;

// ---------------------------------------------------------------------------------//
public abstract class DiskReader
// ---------------------------------------------------------------------------------//
{
  static final int BLOCK_SIZE = 256;
  static final byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };

  static DiskReader reader13;
  static DiskReader reader16;

  final int sectorsPerTrack;

  // ---------------------------------------------------------------------------------//
  DiskReader (int sectorsPerTrack)
  // ---------------------------------------------------------------------------------//
  {
    this.sectorsPerTrack = sectorsPerTrack;
  }

  // ---------------------------------------------------------------------------------//
  static DiskReader getDiskReader (int sectors)
  // ---------------------------------------------------------------------------------//
  {
    if (sectors == 13)
    {
      if (reader13 == null)
        reader13 = new DiskReader13Sector ();
      return reader13;
    }

    if (sectors == 16)
    {
      if (reader16 == null)
        reader16 = new DiskReader16Sector ();
      return reader16;
    }
    return null;
  }

  // ---------------------------------------------------------------------------------//
  byte[] decodeSector (byte[] buffer) throws DiskNibbleException
  // ---------------------------------------------------------------------------------//
  {
    return decodeSector (buffer, 0);
  }

  // ---------------------------------------------------------------------------------//
  // abstract functions
  // ---------------------------------------------------------------------------------//

  abstract byte[] decodeSector (byte[] buffer, int offset) throws DiskNibbleException;

  abstract byte[] encodeSector (byte[] buffer);

  abstract void storeBuffer (RawDiskSector diskSector, byte[] diskBuffer);

  abstract int expectedDataSize ();
}
