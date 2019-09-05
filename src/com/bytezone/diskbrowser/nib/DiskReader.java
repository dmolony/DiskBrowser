package com.bytezone.diskbrowser.nib;

// -----------------------------------------------------------------------------------//
abstract class DiskReader
// -----------------------------------------------------------------------------------//
{
  static final int SECTOR_SIZE = 256;
  static final int BLOCK_SIZE = 512;
  static final byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };

  static DiskReader reader13;
  static DiskReader reader16;
  static DiskReader readerGCR;

  final int sectorsPerTrack;

  // ---------------------------------------------------------------------------------//
  DiskReader (int sectorsPerTrack)
  // ---------------------------------------------------------------------------------//
  {
    this.sectorsPerTrack = sectorsPerTrack;
  }

  // ---------------------------------------------------------------------------------//
  static DiskReader getInstance (int sectors)
  // ---------------------------------------------------------------------------------//
  {
    switch (sectors)
    {
      case 13:
        if (reader13 == null)
          reader13 = new DiskReader13Sector ();
        return reader13;

      case 16:
        if (reader16 == null)
          reader16 = new DiskReader16Sector ();
        return reader16;

      case 0:
        if (readerGCR == null)
          readerGCR = new DiskReaderGCR ();
        return readerGCR;

      default:
        return null;
    }
  }

  // ---------------------------------------------------------------------------------//
  byte[] decodeSector (byte[] buffer) throws DiskNibbleException
  // ---------------------------------------------------------------------------------//
  {
    return decodeSector (buffer, 0);
  }

  // reverse 2 bits - 0 <= bits <= 3
  // ---------------------------------------------------------------------------------//
  static int reverse (int bits)
  // ---------------------------------------------------------------------------------//
  {
    return bits == 1 ? 2 : bits == 2 ? 1 : bits;
  }

  // ---------------------------------------------------------------------------------//
  abstract byte[] decodeSector (byte[] buffer, int offset) throws DiskNibbleException;
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  abstract byte[] encodeSector (byte[] buffer);
  // ---------------------------------------------------------------------------------//
}
