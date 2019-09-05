package com.bytezone.diskbrowser.nib;

// -----------------------------------------------------------------------------------//
public class DiskReaderGCR extends DiskReader
// -----------------------------------------------------------------------------------//
{
  private final ByteTranslator byteTranslator = new ByteTranslator6and2 ();

  // ---------------------------------------------------------------------------------//
  DiskReaderGCR ()
  // ---------------------------------------------------------------------------------//
  {
    super (0);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  byte[] decodeSector (byte[] inBuffer, int inPtr) throws DiskNibbleException
  // ---------------------------------------------------------------------------------//
  {
    byte[] outBuffer = new byte[BLOCK_SIZE * 2 + 12];           // 524 bytes
    int outPtr = 0;
    int[] checksums = new int[3];

    // decode four disk bytes into three data bytes (174 * 3 + 2 = 524)
    while (true)
    {
      // ROL first checksum
      checksums[0] = (checksums[0] & 0xFF) << 1;                // shift left
      if ((checksums[0] > 0xFF))                                // check for overflow
        ++checksums[0];                                         // set bit 0

      // 6&2 translation
      byte d3 = byteTranslator.decode (inBuffer[inPtr++]);      // composite byte
      byte d0 = byteTranslator.decode (inBuffer[inPtr++]);
      byte d1 = byteTranslator.decode (inBuffer[inPtr++]);

      // reassemble bytes
      byte b0 = (byte) (d0 | ((d3 & 0x30) << 2));
      byte b1 = (byte) (d1 | ((d3 & 0x0C) << 4));

      // calculate running checksums
      outBuffer[outPtr++] = checksum (b0, checksums, 0, 2);
      outBuffer[outPtr++] = checksum (b1, checksums, 2, 1);

      if (outPtr == outBuffer.length)
        break;

      byte d2 = byteTranslator.decode (inBuffer[inPtr++]);      // translate
      byte b2 = (byte) (d2 | (d3 << 6));                        // reassemble
      outBuffer[outPtr++] = checksum (b2, checksums, 1, 0);     // checksum
    }

    // decode four disk bytes into three data bytes
    byte d3 = byteTranslator.decode (inBuffer[inPtr++]);        // composite byte
    byte d0 = byteTranslator.decode (inBuffer[inPtr++]);
    byte d1 = byteTranslator.decode (inBuffer[inPtr++]);
    byte d2 = byteTranslator.decode (inBuffer[inPtr++]);

    // reassemble bytes
    byte b0 = (byte) ((d0 & 0x3F) | ((d3 & 0x30) << 2));
    byte b1 = (byte) ((d1 & 0x3F) | ((d3 & 0x0C) << 4));
    byte b2 = (byte) ((d2 & 0x3F) | ((d3 & 0x03) << 6));

    // compare disk checksums with calculated checksums
    if ((checksums[0] & 0xFF) != (b2 & 0xFF)        //
        || (checksums[1] & 0xFF) != (b1 & 0xFF)     //
        || (checksums[2] & 0xFF) != (b0 & 0xFF))
      throw new DiskNibbleException ("Checksum failed");

    return outBuffer;
  }

  // ---------------------------------------------------------------------------------//
  private byte checksum (byte diskByte, int[] checksums, int current, int next)
  // ---------------------------------------------------------------------------------//
  {
    int val = (diskByte ^ checksums[current]) & 0xFF;
    checksums[next] += val;             // prepare next checksum

    if (checksums[current] > 0xFF)      // is there a carry?
    {
      ++checksums[next];                // pass it on
      checksums[current] &= 0xFF;       // back to 8 bits
    }

    return (byte) val;                  // converted data byte
  }

  // ---------------------------------------------------------------------------------//
  @Override
  byte[] encodeSector (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  int expectedDataSize ()
  // ---------------------------------------------------------------------------------//
  {
    assert false;
    return 0;
  }
}
