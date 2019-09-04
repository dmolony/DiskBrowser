package com.bytezone.diskbrowser.nib;

// -----------------------------------------------------------------------------------//
public class DiskReaderGRC extends DiskReader
// -----------------------------------------------------------------------------------//
{
  private final ByteTranslator byteTranslator = new ByteTranslator6and2 ();

  // ---------------------------------------------------------------------------------//
  DiskReaderGRC ()
  // ---------------------------------------------------------------------------------//
  {
    super (0);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  byte[] decodeSector (byte[] buffer, int ptr) throws DiskNibbleException
  // ---------------------------------------------------------------------------------//
  {
    byte[] outBuffer = new byte[BLOCK_SIZE * 2 + 12];       // 524 bytes
    int outPtr = 0;

    int[] checksums = new int[3];

    for (int j = 0; j < 175; j++)
    {
      checksums[0] = (checksums[0] & 0xFF) << 1;            // ROL
      if ((checksums[0] > 0xFF))
        ++checksums[0];

      byte d3 = byteTranslator.decode (buffer[ptr++]);       // composite byte
      byte d0 = byteTranslator.decode (buffer[ptr++]);
      byte d1 = byteTranslator.decode (buffer[ptr++]);

      byte b0 = (byte) ((d0 & 0x3F) | ((d3 & 0x30) << 2));
      byte b1 = (byte) ((d1 & 0x3F) | ((d3 & 0x0C) << 4));

      outBuffer[outPtr++] = checksum (b0, checksums, 0, 2);
      outBuffer[outPtr++] = checksum (b1, checksums, 2, 1);

      if (j < 174)
      {
        byte d2 = byteTranslator.decode (buffer[ptr++]);
        byte b2 = (byte) ((d2 & 0x3F) | ((d3 & 0x03) << 6));
        outBuffer[outPtr++] = checksum (b2, checksums, 1, 0);
      }
    }

    byte d3 = byteTranslator.decode (buffer[ptr++]);         // composite byte
    byte d0 = byteTranslator.decode (buffer[ptr++]);
    byte d1 = byteTranslator.decode (buffer[ptr++]);
    byte d2 = byteTranslator.decode (buffer[ptr++]);

    byte b0 = (byte) ((d0 & 0x3F) | ((d3 & 0x30) << 2));
    byte b1 = (byte) ((d1 & 0x3F) | ((d3 & 0x0C) << 4));
    byte b2 = (byte) ((d2 & 0x3F) | ((d3 & 0x03) << 6));

    if ((checksums[0] & 0xFF) != (b2 & 0xFF)        //
        || (checksums[1] & 0xFF) != (b1 & 0xFF)     //
        || (checksums[2] & 0xFF) != (b0 & 0xFF))
      throw new DiskNibbleException ("Checksum failed");

    return outBuffer;
  }

  // ---------------------------------------------------------------------------------//
  private byte checksum (byte b, int[] checksums, int c1, int c2)
  // ---------------------------------------------------------------------------------//
  {
    int val = (b ^ checksums[c1]) & 0xFF;
    checksums[c2] += val;

    if (checksums[c1] > 0xFF)
    {
      ++checksums[c2];
      checksums[c1] &= 0xFF;
    }

    return (byte) val;
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
