package com.bytezone.diskbrowser.disk;

import com.bytezone.diskbrowser.disk.MC3470.DiskSector;

public class DiskReader16Sector extends DiskReader
{
  private static final int RAW_BUFFER_SIZE_DOS_33 = 342;
  private static final int BUFFER_WITH_CHECKSUM_SIZE_DOS_33 = RAW_BUFFER_SIZE_DOS_33 + 1;

  private final byte[] decodeDos33a = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_33];
  private final byte[] decodeDos33b = new byte[RAW_BUFFER_SIZE_DOS_33];

  private final byte[] encodeDos33a = new byte[RAW_BUFFER_SIZE_DOS_33];
  private final byte[] encodeDos33b = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_33];

  private final ByteTranslator byteTranslator62 = new ByteTranslator6and2 ();

  private static int[] interleave =
      { 0, 8, 1, 9, 2, 10, 3, 11, 4, 12, 5, 13, 6, 14, 7, 15 };

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  DiskReader16Sector ()
  {
    super (16);
  }

  // ---------------------------------------------------------------------------------//
  // decodeSector
  // ---------------------------------------------------------------------------------//

  @Override
  byte[] decodeSector (byte[] buffer, int offset)
  {
    // rearrange 342 bytes into 256
    byte[] decodedBuffer = new byte[BLOCK_SIZE];                      // 256 bytes

    try
    {
      if (offset + BUFFER_WITH_CHECKSUM_SIZE_DOS_33 >= buffer.length)
        throw new DiskNibbleException (
            String.format ("Buffer not long enough (need %d, found %d)",
                BUFFER_WITH_CHECKSUM_SIZE_DOS_33, buffer.length - offset));

      // convert legal disk values to actual 6 bit values
      for (int i = 0; i < BUFFER_WITH_CHECKSUM_SIZE_DOS_33; i++)      // 343 bytes
      {
        if (offset == buffer.length)
          offset = 0;
        decodeDos33a[i] = byteTranslator62.decode (buffer[offset++]);
      }

      // reconstruct 342 bytes each with 6 bits
      byte chk = 0;
      for (int i = decodeDos33b.length - 1; i >= 0; i--)              // 342 bytes
        chk = decodeDos33b[i] = (byte) (decodeDos33a[i + 1] ^ chk);
      if ((chk ^ decodeDos33a[0]) != 0)
        throw new DiskNibbleException ("Checksum failed");

      // move 6 bits into place
      for (int i = 0; i < BLOCK_SIZE; i++)
        decodedBuffer[i] = decodeDos33b[i + 86];

      // reattach each byte's last 2 bits
      for (int i = 0, j = 86, k = 172; i < 86; i++, j++, k++)
      {
        byte val = decodeDos33b[i];

        decodedBuffer[i] |= reverse ((val & 0x0C) >> 2);
        decodedBuffer[j] |= reverse ((val & 0x30) >> 4);

        if (k < BLOCK_SIZE)
          decodedBuffer[k] |= reverse ((val & 0xC0) >> 6);
      }
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

  // convert 256 data bytes into 342 translated bytes plus a checksum
  @Override
  byte[] encodeSector (byte[] buffer)
  {
    byte[] encodedBuffer = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_33];

    // move data buffer down to make room for the 86 extra bytes
    for (int i = 0; i < BLOCK_SIZE; i++)
      encodeDos33a[i + 86] = buffer[i];

    // build extra 86 bytes from the bits stripped from the data bytes
    for (int i = 0; i < 86; i++)
    {
      int b1 = reverse (buffer[i] & 0x03) << 2;
      int b2 = reverse (buffer[i + 86] & 0x03) << 4;

      if (i < 84)
      {
        int b3 = reverse (buffer[i + 172] & 0x03) << 6;
        encodeDos33a[i] = (byte) (b1 | b2 | b3);
      }
      else
        encodeDos33a[i] = (byte) (b1 | b2);
    }

    // convert into checksum bytes
    byte checksum = 0;
    for (int i = 0; i < RAW_BUFFER_SIZE_DOS_33; i++)
    {
      encodeDos33b[i] = (byte) (checksum ^ encodeDos33a[i]);
      checksum = encodeDos33a[i];
    }
    encodeDos33b[RAW_BUFFER_SIZE_DOS_33] = checksum;        // add checksum to the end

    // remove two bits and convert to translated bytes
    for (int i = 0; i < BUFFER_WITH_CHECKSUM_SIZE_DOS_33; i++)
      encodedBuffer[i] = byteTranslator62.encode (encodeDos33b[i]);

    return encodedBuffer;
  }

  // ---------------------------------------------------------------------------------//
  // reverse
  // ---------------------------------------------------------------------------------//

  // reverse 2 bits - 0 <= bits <= 3
  private static int reverse (int bits)
  {
    return bits == 1 ? 2 : bits == 2 ? 1 : bits;
  }

  // ---------------------------------------------------------------------------------//
  // storeBuffer
  // ---------------------------------------------------------------------------------//

  @Override
  void storeBuffer (DiskSector diskSector, byte[] diskBuffer)
  {
    DiskAddressField addressField = diskSector.addressField;
    byte[] sectorBuffer = diskSector.buffer;
    int offset = addressField.track * 0x1000 + interleave[addressField.sector] * 256;
    System.arraycopy (sectorBuffer, 0, diskBuffer, offset, 256);
  }

  // ---------------------------------------------------------------------------------//
  // expectedDataSize
  // ---------------------------------------------------------------------------------//

  @Override
  int expectedDataSize ()
  {
    return 343;
  }
}
