package com.bytezone.diskbrowser.nib;

public class DiskReader13Sector extends DiskReader
{
  private static final int RAW_BUFFER_SIZE = 410;
  private static final int BUFFER_WITH_CHECKSUM_SIZE = RAW_BUFFER_SIZE + 1;

  private final byte[] decodeA = new byte[BUFFER_WITH_CHECKSUM_SIZE];
  private final byte[] decodeB = new byte[RAW_BUFFER_SIZE];

  private final ByteTranslator byteTranslator = new ByteTranslator5and3 ();

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
  byte[] decodeSector (byte[] buffer, int offset) throws DiskNibbleException
  {
    byte[] decodedBuffer = new byte[BLOCK_SIZE];
    //    int offset = 0;

    // convert legal disk values to actual 5 bit values
    for (int i = 0; i < BUFFER_WITH_CHECKSUM_SIZE; i++)             // 411 bytes
      decodeA[i] = (byte) (byteTranslator.decode (buffer[offset++]) << 3);

    // reconstruct 410 bytes each with 5 bits
    byte chk = 0;
    int ptr = 0;
    for (int i = 409; i >= 256; i--)                                // 154 bytes
      chk = decodeB[i] = (byte) (decodeA[ptr++] ^ chk);
    for (int i = 0; i < 256; i++)                                   // 256 bytes
      chk = decodeB[i] = (byte) (decodeA[ptr++] ^ chk);
    if ((chk ^ decodeA[ptr]) != 0)
      throw new DiskNibbleException ("Checksum failed");

    // rearrange 410 bytes into 256
    byte[] k = new byte[8];
    final int[] lines = { 0, 51, 102, 153, 204, 256, 307, 358 };    // 255 is skipped

    // process 8 disk bytes at a time, giving 5 valid bytes
    // do this 51 times, giving 255 bytes
    ptr = 0;
    for (int i = 50; i >= 0; i--)
    {
      for (int j = 0; j < 8; j++)
        k[j] = decodeB[i + lines[j]];

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
    decodedBuffer[ptr] = (byte) (decodeB[255] | ((decodeB[409] & 0x3F) >>> 3));

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

  //  @Override
  //  void storeBuffer (RawDiskSector diskSector, byte[] diskBuffer)
  //  {
  //    DiskAddressField addressField = diskSector.addressField;
  //    byte[] sectorBuffer = diskSector.buffer;
  //    int offset = addressField.track * 0x0D00 + addressField.sector * 256;
  //    System.arraycopy (sectorBuffer, 0, diskBuffer, offset, 256);
  //  }

  // ---------------------------------------------------------------------------------//
  // expectedDataSize
  // ---------------------------------------------------------------------------------//

  @Override
  int expectedDataSize ()
  {
    return BUFFER_WITH_CHECKSUM_SIZE;
  }
}
