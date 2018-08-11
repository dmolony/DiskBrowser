package com.bytezone.diskbrowser.disk;

import com.bytezone.diskbrowser.disk.MC3470.DiskSector;

public class DiskReader13Sector extends DiskReader
{
  private static final int RAW_BUFFER_SIZE_DOS_32 = 410;
  private static final int BUFFER_WITH_CHECKSUM_SIZE_DOS_32 = RAW_BUFFER_SIZE_DOS_32 + 1;

  private final byte[] decodeDos32a = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_32];
  private final byte[] decodeDos32b = new byte[RAW_BUFFER_SIZE_DOS_32];

  private final ByteTranslator byteTranslator53 = new ByteTranslator5and3 ();

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  DiskReader13Sector ()
  {
    super (13);
  }

  // ---------------------------------------------------------------------------------//
  // decodeSector
  // ---------------------------------------------------------------------------------//

  @Override
  byte[] decodeSector (byte[] buffer, int offset)
  {
    byte[] decodedBuffer = new byte[BLOCK_SIZE];

    try
    {
      // convert legal disk values to actual 5 bit values
      for (int i = 0; i < BUFFER_WITH_CHECKSUM_SIZE_DOS_32; i++)        // 411 bytes
        decodeDos32a[i] = byteTranslator53.decode (buffer[offset++]);

      // reconstruct 410 bytes each with 5 bits
      byte chk = 0;
      int ptr = 0;
      for (int i = 409; i >= 256; i--)                                  // 154 bytes
        chk = decodeDos32b[i] = (byte) (decodeDos32a[ptr++] ^ chk);
      for (int i = 0; i < 256; i++)                                     // 256 bytes
        chk = decodeDos32b[i] = (byte) (decodeDos32a[ptr++] ^ chk);
      if ((chk ^ decodeDos32a[ptr]) != 0)
        throw new DiskNibbleException ("Checksum failed");

      // rearrange 410 bytes into 256
      byte[] k = new byte[8];
      final int[] lines = { 0, 51, 102, 153, 204, 256, 307, 358 };   // 255 is skipped

      // process 8 disk bytes at a time, giving 5 valid bytes
      // do this 51 times, giving 255 bytes
      ptr = 0;
      for (int i = 50; i >= 0; i--)
      {
        for (int j = 0; j < 8; j++)
          k[j] = decodeDos32b[i + lines[j]];

        k[0] |= (k[5] & 0xE0) >>> 5;
        k[1] |= (k[6] & 0xE0) >>> 5;
        k[2] |= (k[7] & 0xE0) >>> 5;

        k[3] |= (k[5] & 0x10) >>> 2;
        k[3] |= (k[6] & 0x10) >>> 3;
        k[3] |= (k[7] & 0x10) >>> 4;

        k[4] |= (k[5] & 0x08) >>> 1;
        k[4] |= (k[6] & 0x08) >>> 2;
        k[4] |= (k[7] & 0x08) >>> 3;

        for (int j = 0; j < 5; j++)
          decodedBuffer[ptr++] = k[j];
      }

      // add last byte
      decodedBuffer[ptr] =
          (byte) (decodeDos32b[255] | ((decodeDos32b[409] & 0x3F) >>> 3));
    }
    catch (Exception e)
    {
      System.out.println (e);
    }

    return decodedBuffer;
  }

  // ---------------------------------------------------------------------------------//
  // encodeSector
  // ---------------------------------------------------------------------------------//

  @Override
  byte[] encodeSector (byte[] buffer)
  {
    System.out.println ("encodeSector() not written");
    return null;
  }

  // ---------------------------------------------------------------------------------//
  // storeBuffer
  // ---------------------------------------------------------------------------------//

  @Override
  void storeBuffer (DiskSector diskSector, byte[] diskBuffer)
  {
    DiskAddressField addressField = diskSector.addressField;
    byte[] sectorBuffer = diskSector.buffer;
    int offset = addressField.track * 0x0D00 + addressField.sector * 256;
    System.arraycopy (sectorBuffer, 0, diskBuffer, offset, 256);
  }

  // ---------------------------------------------------------------------------------//
  // expectedDataSize
  // ---------------------------------------------------------------------------------//

  @Override
  int expectedDataSize ()
  {
    return 411;
  }
}
