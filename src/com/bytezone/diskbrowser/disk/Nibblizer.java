package com.bytezone.diskbrowser.disk;

class Nibblizer
{
  // still used by NibDisk and V2dDisk

  private static byte[] addressPrologue32 = { (byte) 0xD5, (byte) 0xAA, (byte) 0xB5 };
  private static byte[] addressPrologue33 = { (byte) 0xD5, (byte) 0xAA, (byte) 0x96 };
  private static byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };
  private static byte[] epilogue = { (byte) 0xDE, (byte) 0xAA, (byte) 0xEB };

  private static int[] interleave =
      { 0, 8, 1, 9, 2, 10, 3, 11, 4, 12, 5, 13, 6, 14, 7, 15 };

  private static final int BLOCK_SIZE = 256;
  private static final int RAW_BUFFER_SIZE_DOS_33 = 342;
  private static final int RAW_BUFFER_SIZE_DOS_32 = 410;
  private static final int BUFFER_WITH_CHECKSUM_SIZE_DOS_33 = RAW_BUFFER_SIZE_DOS_33 + 1;
  private static final int BUFFER_WITH_CHECKSUM_SIZE_DOS_32 = RAW_BUFFER_SIZE_DOS_32 + 1;

  private final ByteTranslator byteTranslator62 = new ByteTranslator6and2 ();
  private final ByteTranslator byteTranslator53 = new ByteTranslator5and3 ();

  private final byte[] decodeDos33a = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_33];
  private final byte[] decodeDos33b = new byte[RAW_BUFFER_SIZE_DOS_33];

  private final byte[] encodeDos33a = new byte[RAW_BUFFER_SIZE_DOS_33];
  private final byte[] encodeDos33b = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_33];

  private final byte[] decodeDos32a = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_32];
  private final byte[] decodeDos32b = new byte[RAW_BUFFER_SIZE_DOS_32];

  private int sectorsPerTrack;

  // ---------------------------------------------------------------------------------//
  // processTrack
  // ---------------------------------------------------------------------------------//

  boolean processTrack (int trackNo, int maxTracks, byte[] buffer, byte[] diskBuffer)
  {
    int ptr = 0;
    int totalSectors = 0;
    boolean[] sectorsFound = new boolean[16];
    sectorsPerTrack = maxTracks;

    try
    {
      while (ptr < buffer.length)
      {
        if (sectorsPerTrack == 13)
          ptr = findBytes (buffer, ptr, addressPrologue32);
        else
          ptr = findBytes (buffer, ptr, addressPrologue33);

        if (ptr < 0)
        {
          System.out.printf ("Track: %02X - Address prologue not found%n", trackNo);
          return false;
        }

        AddressField addressField = new AddressField (buffer, ptr);
        if (!addressField.isValid ())
        {
          System.out.printf ("Track: %02X - Invalid address field%n", trackNo);
          return false;
        }

        if (addressField.track != trackNo)
        {
          System.out.printf ("Track: %02X - Wrong track found (%02X)%n", trackNo,
              addressField.track);
          return false;
        }

        if (sectorsFound[addressField.sector])
        {
          System.out.printf ("Track: %02X - Sector already processes (%02X)%n", trackNo,
              addressField.sector);
          return false;
        }
        sectorsFound[addressField.sector] = true;

        assert addressField.track == trackNo;

        ptr += addressField.size ();
        ptr = findBytes (buffer, ptr, dataPrologue);
        if (ptr < 0)
        {
          System.out.printf ("Track: %02X - Data prologue not found%n", trackNo);
          return false;
        }

        DataField dataField = new DataField (buffer, ptr);
        if (!dataField.isValid ())
        {
          System.out.printf ("Track: %02X - Invalid data field%n", trackNo);
          return false;
        }

        int offset;
        if (sectorsPerTrack == 13)
          offset = addressField.track * 0x0D00 + addressField.sector * BLOCK_SIZE;
        else
          offset =
              addressField.track * 0x1000 + interleave[addressField.sector] * BLOCK_SIZE;

        System.arraycopy (dataField.dataBuffer, 0, diskBuffer, offset, BLOCK_SIZE);

        if (++totalSectors == sectorsPerTrack)
          break;

        ptr += dataField.size ();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }

    if (totalSectors != sectorsPerTrack)
    {
      System.out.printf ("Track: %02X - Sectors found: %02X%n", trackNo, totalSectors);
      return false;
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // decode4and4
  // ---------------------------------------------------------------------------------//

  private int decode4and4 (byte[] buffer, int offset)
  {
    int odds = ((buffer[offset] & 0xFF) << 1) + 1;
    int evens = buffer[offset + 1] & 0xFF;
    return odds & evens;
  }

  // ---------------------------------------------------------------------------------//
  // decode5and3
  // ---------------------------------------------------------------------------------//

  private byte[] decode5and3 (byte[] buffer, int offset)
  {
    // rearrange 410 bytes into 256
    byte[] decodedBuffer = new byte[BLOCK_SIZE];                      // 256 bytes

    try
    {
      // convert legal disk values to actual 5 bit values
      for (int i = 0; i < BUFFER_WITH_CHECKSUM_SIZE_DOS_32; i++)        // 411 bytes
      {
        //      System.out.printf ("%,5d  %02X%n", i, buffer[offset]);
        decodeDos32a[i] = (byte) (byteTranslator53.decode (buffer[offset++]) << 3);
      }

      // reconstruct 410 bytes each with 5 bits
      byte chk = 0;
      int ptr = 0;
      for (int i = 409; i >= 256; i--)                                  // 154 bytes
        chk = decodeDos32b[i] = (byte) (decodeDos32a[ptr++] ^ chk);
      for (int i = 0; i < 256; i++)                                     // 256 bytes
        chk = decodeDos32b[i] = (byte) (decodeDos32a[ptr++] ^ chk);
      assert (chk ^ decodeDos32a[ptr]) == 0;

      byte[] k = new byte[8];
      ptr = 0;
      final int[] lines = { 0, 51, 102, 153, 204, 256, 307, 358 };      // 255 is skipped

      // process 8 disk bytes at a time, giving 5 valid bytes
      // do this 51 times, giving 255 bytes
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
      decodedBuffer[255] = (byte) (decodeDos32b[255] | (decodeDos32b[409] >>> 3));
    }
    catch (Exception e)
    {

    }

    return decodedBuffer;
  }

  // ---------------------------------------------------------------------------------//
  // decode6and2
  // ---------------------------------------------------------------------------------//

  private byte[] decode6and2 (byte[] buffer, int offset)
  {
    // rearrange 342 bytes into 256
    byte[] decodedBuffer = new byte[BLOCK_SIZE];                    // 256 bytes

    try
    {
      // convert legal disk values to actual 6 bit values
      for (int i = 0; i < BUFFER_WITH_CHECKSUM_SIZE_DOS_33; i++)      // 343 bytes
        decodeDos33a[i] = (byte) (byteTranslator62.decode (buffer[offset++]) << 2);

      // reconstruct 342 bytes each with 6 bits
      byte chk = 0;
      for (int i = decodeDos33b.length - 1; i >= 0; i--)              // 342 bytes
        chk = decodeDos33b[i] = (byte) (decodeDos33a[i + 1] ^ chk);
      assert (chk ^ decodeDos33a[0]) == 0;

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

    }

    return decodedBuffer;
  }

  // ---------------------------------------------------------------------------------//
  // encode6and2
  // ---------------------------------------------------------------------------------//

  // convert 256 data bytes into 342 translated bytes plus a checksum
  private byte[] encode6and2 (byte[] buffer)
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
      //      encodedBuffer[i] = writeTranslateTable6and2[(encodeDos33b[i] & 0xFC) / 4];
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
  // listBytes
  // ---------------------------------------------------------------------------------//

  private String listBytes (byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();

    int max = Math.min (length + offset, buffer.length);
    while (offset < max)
      text.append (String.format ("%02X ", buffer[offset++]));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // findBytes
  // ---------------------------------------------------------------------------------//

  static int findBytes (byte[] buffer, int offset, byte[] valueBuffer)
  {
    while (offset + valueBuffer.length <= buffer.length)
    {
      if (matchBytes (buffer, offset, valueBuffer))
        return offset;
      ++offset;
    }

    return -1;
  }

  // ---------------------------------------------------------------------------------//
  // matchBytes
  // ---------------------------------------------------------------------------------//

  private static boolean matchBytes (byte[] buffer, int offset, byte[] valueBuffer)
  {
    if ((buffer.length - offset) < valueBuffer.length)
      return false;

    int ptr = 0;

    try
    {
      while (ptr < valueBuffer.length)
        if (buffer[offset++] != valueBuffer[ptr++])
          return false;
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      System.out.println ("Error in matchBytes");
      e.printStackTrace ();
      return false;
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // Field
  // ---------------------------------------------------------------------------------//

  private abstract class Field
  {
    protected boolean valid;
    protected byte[] buffer;
    protected int offset;
    protected int length;

    public Field (byte[] buffer, int offset)
    {
      this.buffer = buffer;
      this.offset = offset;
    }

    public boolean isValid ()
    {
      return valid;
    }

    public int size ()
    {
      assert length > 0;
      return length;
    }

    @Override
    public String toString ()
    {
      return String.format ("[Offset: %04X, Length: %04X]", offset, length);
    }
  }

  // ---------------------------------------------------------------------------------//
  // AddressField
  // ---------------------------------------------------------------------------------//

  private class AddressField extends Field
  {
    int track, sector, volume, checksum;

    public AddressField (byte[] buffer, int offset)
    {
      super (buffer, offset);

      volume = decode4and4 (buffer, offset + 3);
      track = decode4and4 (buffer, offset + 5);
      sector = decode4and4 (buffer, offset + 7);
      checksum = decode4and4 (buffer, offset + 9);
      valid = true;

      length = 14;
    }

    @Override
    public String toString ()
    {
      return String.format ("[volume: %02X, track: %02X, sector: %02X, checksum: %02X]",
          volume, track, sector, checksum);
    }
  }

  // ---------------------------------------------------------------------------------//
  // DataField
  // ---------------------------------------------------------------------------------//

  private class DataField extends Field
  {
    byte[] dataBuffer;

    public DataField (byte[] buffer, int offset)
    {
      super (buffer, offset);

      if (matchBytes (buffer, offset, dataPrologue))
      {
        valid = true;
        if (sectorsPerTrack == 13)
          dataBuffer = decode5and3 (buffer, offset + 3);
        else
          dataBuffer = decode6and2 (buffer, offset + 3);
      }
      else
      {
        System.out.print ("   bad data prologue: ");
        System.out.println (listBytes (buffer, offset, 3));
      }

      length = 349;
    }
  }
}