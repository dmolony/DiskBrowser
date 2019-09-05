package com.bytezone.diskbrowser.nib;

// -----------------------------------------------------------------------------------//
public class DiskReaderGCR extends DiskReader
// -----------------------------------------------------------------------------------//
{
  static final int TAG_SIZE = 12;
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
    byte[] outBuffer = new byte[BLOCK_SIZE + TAG_SIZE];         // 524 bytes
    int outPtr = 0;
    int[] checksums = new int[3];

    // decode four disk bytes into three data bytes (174 * 3 + 2 = 524)
    while (true)
    {
      // ROL checksum
      checksums[2] = (checksums[2] & 0xFF) << 1;                // shift left
      if ((checksums[2] > 0xFF))                                // check for overflow
        ++checksums[2];                                         // set bit 0

      // 6&2 translation
      byte d3 = byteTranslator.decode (inBuffer[inPtr++]);      // composite byte
      byte d0 = byteTranslator.decode (inBuffer[inPtr++]);
      byte d1 = byteTranslator.decode (inBuffer[inPtr++]);

      // reassemble data bytes
      byte b0 = (byte) (d0 | ((d3 << 2) & 0xC0));
      byte b1 = (byte) (d1 | ((d3 << 4) & 0xC0));

      // calculate running checksums
      outBuffer[outPtr++] = checksum (b0, checksums, 2, 0);
      outBuffer[outPtr++] = checksum (b1, checksums, 0, 1);

      if (outPtr == outBuffer.length)
        break;

      byte d2 = byteTranslator.decode (inBuffer[inPtr++]);      // translate
      byte b2 = (byte) (d2 | (d3 << 6));                        // reassemble
      outBuffer[outPtr++] = checksum (b2, checksums, 1, 2);     // checksum
    }

    // decode four disk bytes into three checksum bytes
    byte d3 = byteTranslator.decode (inBuffer[inPtr++]);        // composite byte
    byte d0 = byteTranslator.decode (inBuffer[inPtr++]);
    byte d1 = byteTranslator.decode (inBuffer[inPtr++]);
    byte d2 = byteTranslator.decode (inBuffer[inPtr++]);

    // reassemble checksums
    byte b0 = (byte) (d0 | ((d3 << 2) & 0xC0));
    byte b1 = (byte) (d1 | ((d3 << 4) & 0xC0));
    byte b2 = (byte) (d2 | (d3 << 6));

    // compare disk checksums with calculated checksums
    if ((checksums[0] & 0xFF) != (b0 & 0xFF)        //
        || (checksums[1] & 0xFF) != (b1 & 0xFF)     //
        || (checksums[2] & 0xFF) != (b2 & 0xFF))
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
    System.out.println ("encodeSector() not written");
    return null;
  }
}
