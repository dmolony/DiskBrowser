package com.bytezone.diskbrowser.disk;

import java.io.File;

public class Nibblizer
{
  static byte[] addressPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0x96 };
  static byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };
  static byte[] epilogue = { (byte) 0xDE, (byte) 0xAA, (byte) 0xEB };

  private static int[][] interleave =
      { { 0, 8, 1, 9, 2, 10, 3, 11, 4, 12, 5, 13, 6, 14, 7, 15 },
        { 0, 8, 1, 9, 2, 10, 3, 11, 4, 12, 5, 13, 6, 14, 7, 15 } };

  private static final int DOS = 0;
  private static final int PRODOS = 1;
  private static final int BLOCK_SIZE = 256;
  private static final int TRACK_SIZE = 4096;
  private static final int RAW_BUFFER_SIZE = 342;
  private static final int BUFFER_WITH_CHECKSUM_SIZE = RAW_BUFFER_SIZE + 1;

  private static byte[] writeTranslateTable =
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

  private static byte[] readTranslateTable = new byte[106];   // skip first 150 blanks

  // this array is just here for testing - it matches the example in Beneath Apple Prodos
  private static byte[] testData =
      { (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0xFA, (byte) 0x55,
        (byte) 0x53, (byte) 0x45, (byte) 0x52, (byte) 0x53, (byte) 0x2E, (byte) 0x44,
        (byte) 0x49, (byte) 0x53, (byte) 0x4B, (byte) 0x00, //
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, //
        (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x27, (byte) 0x0D, (byte) 0x09,
        (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x18, (byte) 0x01, (byte) 0x26,
        (byte) 0x50, (byte) 0x52, (byte) 0x4F, (byte) 0x44, //
        (byte) 0x4F, (byte) 0x53, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
        (byte) 0x08, (byte) 0x00, (byte) 0x1F, (byte) 0x00, //

        (byte) 0x00, (byte) 0x3C, (byte) 0x00, (byte) 0x21, (byte) 0xA8, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x21, (byte) 0x00, (byte) 0x20,
        (byte) 0x21, (byte) 0xA8, (byte) 0x00, (byte) 0x00, //
        (byte) 0x02, (byte) 0x00, (byte) 0x2C, (byte) 0x42, (byte) 0x41, (byte) 0x53,
        (byte) 0x49, (byte) 0x43, (byte) 0x2E, (byte) 0x53, (byte) 0x59, (byte) 0x53,
        (byte) 0x54, (byte) 0x45, (byte) 0x4D, (byte) 0x00, //
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x27, (byte) 0x00, (byte) 0x15,
        (byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0x00, (byte) 0x6F, (byte) 0xA7,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, //
        (byte) 0x21, (byte) 0x00, (byte) 0x20, (byte) 0x6F, (byte) 0xA7, (byte) 0x00,
        (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x25, (byte) 0x46, (byte) 0x49,
        (byte) 0x4C, (byte) 0x45, (byte) 0x52, (byte) 0x00, //

        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x3C, (byte) 0x00,
        (byte) 0x33, (byte) 0x00, (byte) 0x00, (byte) 0x64, //
        (byte) 0x00, (byte) 0x21, (byte) 0xA8, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x21, (byte) 0x6E, (byte) 0x01, (byte) 0x21, (byte) 0xA8,
        (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, //
        (byte) 0x27, (byte) 0x43, (byte) 0x4F, (byte) 0x4E, (byte) 0x56, (byte) 0x45,
        (byte) 0x52, (byte) 0x54, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, //
        (byte) 0xFF, (byte) 0x6F, (byte) 0x00, (byte) 0x2A, (byte) 0x00, (byte) 0x01,
        (byte) 0x50, (byte) 0x00, (byte) 0x61, (byte) 0xA7, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x21, (byte) 0x00, //

        (byte) 0x20, (byte) 0x61, (byte) 0xA7, (byte) 0x00, (byte) 0x00, (byte) 0x02,
        (byte) 0x00, (byte) 0x27, (byte) 0x53, (byte) 0x54, (byte) 0x41, (byte) 0x52,
        (byte) 0x54, (byte) 0x55, (byte) 0x50, (byte) 0x00, //
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0xFC, (byte) 0x99, (byte) 0x00, (byte) 0x18, (byte) 0x00,
        (byte) 0xC9, (byte) 0x2C, (byte) 0x00, (byte) 0x4F, //
        (byte) 0xA7, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x21,
        (byte) 0x01, (byte) 0x08, (byte) 0x4F, (byte) 0xA7, (byte) 0x00, (byte) 0x00,
        (byte) 0x02, (byte) 0x00, (byte) 0x25, (byte) 0x4D, //
        (byte) 0x4F, (byte) 0x49, (byte) 0x52, (byte) 0x45, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0xFC, (byte) 0xB1 };

  static
  {
    for (int i = 0; i < writeTranslateTable.length; i++)
    {
      int j = (writeTranslateTable[i] & 0xFF) - 150;      // skip first 150 blanks
      readTranslateTable[j] = (byte) (i + 1);             // offset by 1 to avoid zero
    }
  }

  private final byte[] decode1 = new byte[BUFFER_WITH_CHECKSUM_SIZE];
  private final byte[] decode2 = new byte[RAW_BUFFER_SIZE];

  private final byte[] encode1 = new byte[RAW_BUFFER_SIZE];
  private final byte[] encode2 = new byte[BUFFER_WITH_CHECKSUM_SIZE];

  private final File file;

  public Nibblizer (File file)
  {
    this.file = file;

    if (false)      // test with the Beneath Apple Prodos example
    {
      byte[] testBuffer = decode6and2 (encode6and2 (testData), 0);

      for (int i = 0; i < BLOCK_SIZE; i++)
        if (testData[i] != testBuffer[i])
          System.out.println ("bollocks");

      return;
    }
  }

  public boolean processTrack (int trackNo, byte[] buffer, byte[] diskBuffer)
  {
    int ptr = 0;

    while (buffer[ptr] == (byte) 0xEB)
    {
      System.out.printf ("%s overrun 0xEB offset %d in track %02X%n", file.getName (),
          ptr, trackNo);
      ++ptr;
    }

    ptr += skipBytes (buffer, ptr, (byte) 0xFF);          // gap1

    while (ptr < buffer.length)
    {
      AddressField addressField = getAddressField (buffer, ptr);
      if (!addressField.isValid ())
        return false;

      assert addressField.track == trackNo;

      ptr += addressField.size ();
      ptr += skipBytes (buffer, ptr, (byte) 0xFF);        // gap2

      DataField dataField = getDataField (buffer, ptr);
      if (!dataField.isValid ())
        return false;

      int offset = addressField.track * TRACK_SIZE
          + interleave[DOS][addressField.sector] * BLOCK_SIZE;
      System.arraycopy (dataField.dataBuffer, 0, diskBuffer, offset, BLOCK_SIZE);

      ptr += dataField.size ();
      ptr += skipBytes (buffer, ptr, (byte) 0xFF);        // gap3
    }

    return true;
  }

  private AddressField getAddressField (byte[] buffer, int offset)
  {
    return new AddressField (buffer, offset);
  }

  private DataField getDataField (byte[] buffer, int offset)
  {
    return new DataField (buffer, offset);
  }

  private int decode4and4 (byte[] buffer, int offset)
  {
    int odds = ((buffer[offset] & 0xFF) << 1) + 1;
    int evens = buffer[offset + 1] & 0xFF;
    return odds & evens;
  }

  private byte[] decode6and2 (byte[] buffer, int offset)
  {
    for (int i = 0; i < decode1.length; i++)
    {
      int val = (buffer[offset++] & 0xFF) - 150;
      byte trans = readTranslateTable[val];
      assert trans != 0;
      decode1[i] = (byte) ((trans - 1) << 2);       // readjust by 1 (see above)
    }

    byte chk = 0;
    for (int i = RAW_BUFFER_SIZE; i > 0; i--)
    {
      decode2[i - 1] = (byte) (decode1[i] ^ chk);
      chk = decode2[i - 1];
    }

    byte[] decodedBuffer = new byte[BLOCK_SIZE];

    for (int i = 0; i < BLOCK_SIZE; i++)
      decodedBuffer[i] = decode2[i + 86];

    for (int i = 0; i < 86; i++)
    {
      byte val = decode2[i];

      decodedBuffer[i] |= reverse ((val & 0x0C) >> 2);
      decodedBuffer[i + 86] |= reverse ((val & 0x30) >> 4);

      if (i < 84)
        decodedBuffer[i + 172] |= reverse ((val & 0xC0) >> 6);
    }

    return decodedBuffer;
  }

  // convert 256 data bytes into 342 translated bytes plus a checksum
  private byte[] encode6and2 (byte[] buffer)
  {
    byte[] encodedBuffer = new byte[BUFFER_WITH_CHECKSUM_SIZE];

    // move data buffer down to make room for the 86 extra bytes
    for (int i = 0; i < BLOCK_SIZE; i++)
      encode1[i + 86] = buffer[i];

    // build extra 86 bytes from the bits stripped from the data bytes
    for (int i = 0; i < 86; i++)
    {
      int b1 = reverse (buffer[i] & 0x03) << 2;
      int b2 = reverse (buffer[i + 86] & 0x03) << 4;

      if (i < 84)
      {
        int b3 = reverse (buffer[i + 172] & 0x03) << 6;
        encode1[i] = (byte) (b1 | b2 | b3);
      }
      else
        encode1[i] = (byte) (b1 | b2);
    }

    // convert into checksum bytes
    byte checksum = 0;
    for (int i = 0; i < RAW_BUFFER_SIZE; i++)
    {
      encode2[i] = (byte) (checksum ^ encode1[i]);
      checksum = encode1[i];
    }
    encode2[RAW_BUFFER_SIZE] = checksum;        // add checksum to the end

    // remove two bits and convert to translated bytes
    for (int i = 0; i < BUFFER_WITH_CHECKSUM_SIZE; i++)
      encodedBuffer[i] = writeTranslateTable[(encode2[i] & 0xFC) / 4];

    return encodedBuffer;
  }

  // reverse 2 bits - 0 <= bits <= 3
  private static int reverse (int bits)
  {
    return bits == 1 ? 2 : bits == 2 ? 1 : bits;
  }

  private int skipBytes (byte[] buffer, int offset, byte skipValue)
  {
    int count = 0;
    while (offset < buffer.length && buffer[offset++] == skipValue)
      ++count;
    return count;
  }

  private String listBytes (byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();

    int max = Math.min (length + offset, buffer.length);
    while (offset < max)
      text.append (String.format ("%02X ", buffer[offset++]));

    return text.toString ();
  }

  static int findBytes (byte[] buffer, int offset, byte[] valueBuffer)
  {
    while (offset + valueBuffer.length < buffer.length)
    {
      if (matchBytes (buffer, offset, valueBuffer))
        return offset;
      ++offset;
    }

    return -1;
  }

  private static boolean matchBytes (byte[] buffer, int offset, byte[] valueBuffer)
  {
    if (buffer.length - offset < valueBuffer.length)
      return false;

    int ptr = 0;
    while (ptr < valueBuffer.length)
      if (buffer[offset++] != valueBuffer[ptr++])
        return false;

    return true;
  }

  abstract class Field
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
  }

  class AddressField extends Field
  {
    int track, sector, volume, checksum;

    public AddressField (byte[] buffer, int offset)
    {
      super (buffer, offset);

      if (matchBytes (buffer, offset, addressPrologue)
          && matchBytes (buffer, offset + 11, epilogue))
      {
        volume = decode4and4 (buffer, offset + 3);
        track = decode4and4 (buffer, offset + 5);
        sector = decode4and4 (buffer, offset + 7);
        checksum = decode4and4 (buffer, offset + 9);
        valid = true;
      }
      else
        System.out.println (listBytes (buffer, offset, 14));

      length = 14;
    }
  }

  class DataField extends Field
  {
    byte[] dataBuffer;

    public DataField (byte[] buffer, int offset)
    {
      super (buffer, offset);

      if (matchBytes (buffer, offset, dataPrologue))
      {
        valid = true;
        dataBuffer = decode6and2 (buffer, offset + 3);
        if (!matchBytes (buffer, offset + 3 + BUFFER_WITH_CHECKSUM_SIZE, epilogue))
        {
          System.out.print ("   bad data epilogue: ");
          System.out
              .println (listBytes (buffer, offset + 3 + BUFFER_WITH_CHECKSUM_SIZE, 3));
        }
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