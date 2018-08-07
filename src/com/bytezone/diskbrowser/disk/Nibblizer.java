package com.bytezone.diskbrowser.disk;

class Nibblizer
{
  private static byte[] addressPrologue32 = { (byte) 0xD5, (byte) 0xAA, (byte) 0xB5 };
  private static byte[] addressPrologue33 = { (byte) 0xD5, (byte) 0xAA, (byte) 0x96 };
  private static byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };
  private static byte[] epilogue = { (byte) 0xDE, (byte) 0xAA, (byte) 0xEB };

  private static int[] interleave =
      { 0, 8, 1, 9, 2, 10, 3, 11, 4, 12, 5, 13, 6, 14, 7, 15 };

  private static final int BLOCK_SIZE = 256;
  private static final int TRACK_SIZE = 4096;
  private static final int RAW_BUFFER_SIZE_DOS_33 = 342;
  private static final int RAW_BUFFER_SIZE_DOS_32 = 410;
  private static final int BUFFER_WITH_CHECKSUM_SIZE_DOS_33 = RAW_BUFFER_SIZE_DOS_33 + 1;
  private static final int BUFFER_WITH_CHECKSUM_SIZE_DOS_32 = RAW_BUFFER_SIZE_DOS_32 + 1;

  // 32 valid bytes that can be stored on a disk (plus 0xAA and 0xD5)
  private static byte[] writeTranslateTable5and3 =
      { (byte) 0xAB, (byte) 0xAD, (byte) 0xAE, (byte) 0xAF, (byte) 0xB5, (byte) 0xB6,
        (byte) 0xB7, (byte) 0xBA, (byte) 0xBB, (byte) 0xBD, (byte) 0xBE, (byte) 0xBF,
        (byte) 0xD6, (byte) 0xD7, (byte) 0xDA, (byte) 0xDB, //
        (byte) 0xDD, (byte) 0xDE, (byte) 0xDF, (byte) 0xEA, (byte) 0xEB, (byte) 0xED,
        (byte) 0xEE, (byte) 0xEF, (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xFA,
        (byte) 0xFB, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF };

  // 64 valid bytes that can be stored on a disk (plus 0xAA and 0xD5)
  private static byte[] writeTranslateTable6and2 =
      { (byte) 0x96, (byte) 0x97, (byte) 0x9A, (byte) 0x9B, (byte) 0x9D, (byte) 0x9E,
        (byte) 0x9F, (byte) 0xA6, (byte) 0xA7, (byte) 0xAB, (byte) 0xAC, (byte) 0xAD,
        (byte) 0xAE, (byte) 0xAF, (byte) 0xB2, (byte) 0xB3, //
        (byte) 0xB4, (byte) 0xB5, (byte) 0xB6, (byte) 0xB7, (byte) 0xB9, (byte) 0xBA,
        (byte) 0xBB, (byte) 0xBC, (byte) 0xBD, (byte) 0xBE, (byte) 0xBF, (byte) 0xCB,
        (byte) 0xCD, (byte) 0xCE, (byte) 0xCF, (byte) 0xD3, //
        (byte) 0xD6, (byte) 0xD7, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB, (byte) 0xDC,
        (byte) 0xDD, (byte) 0xDE, (byte) 0xDF, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7,
        (byte) 0xE9, (byte) 0xEA, (byte) 0xEB, (byte) 0xEC, //
        (byte) 0xED, (byte) 0xEE, (byte) 0xEF, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4,
        (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xF9, (byte) 0xFA, (byte) 0xFB,
        (byte) 0xFC, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF };

  private static byte[] readTranslateTable5and3 = new byte[85];   // skip first 171 blanks
  private static byte[] readTranslateTable6and2 = new byte[106];  // skip first 150 blanks

  static
  {
    for (int i = 0; i < writeTranslateTable5and3.length; i++)
    {
      int j = (writeTranslateTable5and3[i] & 0xFF) - 0xAB;   // skip first 171 blanks
      readTranslateTable5and3[j] = (byte) (i + 1);           // offset by 1 to avoid zero
    }

    for (int i = 0; i < writeTranslateTable6and2.length; i++)
    {
      int j = (writeTranslateTable6and2[i] & 0xFF) - 0x96;   // skip first 150 blanks
      readTranslateTable6and2[j] = (byte) (i + 1);           // offset by 1 to avoid zero
    }
  }

  private final byte[] decodeDos33a = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_33];
  private final byte[] decodeDos33b = new byte[RAW_BUFFER_SIZE_DOS_33];

  private final byte[] encodeDos33a = new byte[RAW_BUFFER_SIZE_DOS_33];
  private final byte[] encodeDos33b = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_33];

  private final byte[] decodeDos32a = new byte[BUFFER_WITH_CHECKSUM_SIZE_DOS_32];
  private final byte[] decodeDos32b = new byte[RAW_BUFFER_SIZE_DOS_32];

  enum DosVersion
  {
    DOS_3_2, DOS_3_3
  }

  private DosVersion currentDosVersion;
  int sectorsPerTrack;

  // ---------------------------------------------------------------------------------//
  // processTrack
  // ---------------------------------------------------------------------------------//

  boolean processTrack (int trackNo, byte[] buffer, byte[] diskBuffer)
  {
    int ptr = 0;
    int totalSectors = 0;
    boolean[] sectorsFound = new boolean[16];

    try
    {
      while (ptr < buffer.length)
      {
        int ptr2 = findBytes (buffer, ptr, addressPrologue33);
        if (ptr2 >= 0)
        {
          currentDosVersion = DosVersion.DOS_3_3;
          sectorsPerTrack = 16;
        }
        else
        {
          ptr2 = findBytes (buffer, ptr, addressPrologue32);
          if (ptr2 >= 0)
          {
            currentDosVersion = DosVersion.DOS_3_2;
            sectorsPerTrack = 13;
          }
          else
          {
            System.out.printf ("Track: %02X/%02X - Address prologue not found%n", trackNo,
                totalSectors);
            return false;
          }
        }
        ptr = ptr2;

        AddressField addressField = getAddressField (buffer, ptr);
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

        DataField dataField = getDataField (buffer, ptr);
        if (!dataField.isValid ())
        {
          System.out.printf ("Track: %02X - Invalid data field%n", trackNo);
          return false;
        }

        int offset = addressField.track * TRACK_SIZE;
        if (currentDosVersion == DosVersion.DOS_3_2)
          offset += addressField.sector * BLOCK_SIZE;
        else
          offset += interleave[addressField.sector] * BLOCK_SIZE;

        System.arraycopy (dataField.dataBuffer, 0, diskBuffer, offset, BLOCK_SIZE);

        ++totalSectors;

        if (currentDosVersion == DosVersion.DOS_3_2 && totalSectors == 13)
          break;
        if (currentDosVersion == DosVersion.DOS_3_3 && totalSectors == 16)
          break;

        ptr += dataField.size ();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }

    if (currentDosVersion == DosVersion.DOS_3_2 && totalSectors != 13)
    {
      System.out.printf ("Track: %02X - Sectors found: %02X%n", trackNo, totalSectors);
      return false;
    }

    if (currentDosVersion == DosVersion.DOS_3_3 && totalSectors != 16)
    {
      System.out.printf ("Track: %02X - Sectors found: %02X%n", trackNo, totalSectors);
      return false;
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // getAddressField
  // ---------------------------------------------------------------------------------//

  private AddressField getAddressField (byte[] buffer, int offset)
  {
    return new AddressField (buffer, offset);
  }

  // ---------------------------------------------------------------------------------//
  // getDataField
  // ---------------------------------------------------------------------------------//

  private DataField getDataField (byte[] buffer, int offset)
  {
    return new DataField (buffer, offset);
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
    for (int i = 0; i <= 410; i++)
      decodeDos32a[i] = getByte (buffer[offset++]);

    // reconstruct 410 bytes each with 5 bits
    byte chk = 0;
    int ptr = 0;
    for (int i = 409; i >= 256; i--)                                  // 154 bytes
      chk = decodeDos32b[i] = (byte) (decodeDos32a[ptr++] ^ chk);
    for (int i = 0; i < 256; i++)                                     // 256 bytes
      chk = decodeDos32b[i] = (byte) (decodeDos32a[ptr++] ^ chk);
    assert (chk ^ decodeDos32a[ptr]) == 0;

    // rearrange 410 bytes into 256
    byte[] decodedBuffer = new byte[BLOCK_SIZE];
    byte[] k = new byte[8];
    ptr = 0;
    int[] lines = { 0, 51, 102, 153, 204, 256, 307, 358 };

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

    // last byte not yet tested
    decodedBuffer[255] = (byte) (decodeDos32b[255] | (decodeDos32b[409] >>> 3));

    return decodedBuffer;
  }

  private byte getByte (byte b)
  {
    int val = (b & 0xFF) - 0xAB;                   // 0 - 84
    assert val >= 0 && val <= 84;
    byte trans = (byte) (readTranslateTable5and3[val] - 1);     // 0 - 31  (5 bits)
    assert trans >= 0 && trans <= 31;
    return (byte) (trans << 3);                                 // left justify 5 bits
  }

  // ---------------------------------------------------------------------------------//
  // decode6and2
  // ---------------------------------------------------------------------------------//

  private byte[] decode6and2 (byte[] buffer, int offset)
  {
    // convert legal disk values to actual 6 bit values
    for (int i = 0; i < decodeDos33a.length; i++)                 // 343 bytes
    {
      int val = (buffer[offset++] & 0xFF) - 0x96;                 // 0 - 105
      assert val >= 0 && val <= 105;
      byte trans = (byte) (readTranslateTable6and2[val] - 1);     // 0 - 63  (6 bits)
      assert trans >= 0 && trans <= 63;
      decodeDos33a[i] = (byte) (trans << 2);                      // left-justify 6 bits
    }

    // reconstruct 342 bytes each with 6 bits
    byte chk = 0;
    for (int i = decodeDos33b.length - 1; i >= 0; i--)              // 342 bytes
      chk = decodeDos33b[i] = (byte) (decodeDos33a[i + 1] ^ chk);
    assert (chk ^ decodeDos33a[0]) == 0;

    // rearrange 342 bytes into 256
    byte[] decodedBuffer = new byte[BLOCK_SIZE];                    // 256 bytes

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
      encodedBuffer[i] = writeTranslateTable6and2[(encodeDos33b[i] & 0xFC) / 4];

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

      //      if (matchBytes (buffer, offset, addressPrologue33))
      //      //          && matchBytes (buffer, offset + 11, epilogue))
      //      {
      volume = decode4and4 (buffer, offset + 3);
      track = decode4and4 (buffer, offset + 5);
      sector = decode4and4 (buffer, offset + 7);
      checksum = decode4and4 (buffer, offset + 9);
      valid = true;
      //      }
      //      else
      //        System.out.println (listBytes (buffer, offset, 14));

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
        if (currentDosVersion == DosVersion.DOS_3_3)
          dataBuffer = decode6and2 (buffer, offset + 3);
        else if (currentDosVersion == DosVersion.DOS_3_2)
          dataBuffer = decode5and3 (buffer, offset + 3);
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