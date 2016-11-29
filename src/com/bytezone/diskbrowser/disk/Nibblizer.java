package com.bytezone.diskbrowser.disk;

public class Nibblizer
{
  private static byte[] addressPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0x96 };
  private static byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };
  private static byte[] epilogue = { (byte) 0xDE, (byte) 0xAA, (byte) 0xEB };

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

  private static byte[] readTranslateTable = new byte[106];

  private static byte[] xor =
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
      int j = (writeTranslateTable[i] & 0xFF) - 150;
      readTranslateTable[j] = (byte) (i + 1);
    }
  }

  public Nibblizer ()
  {
    if (false)
    {
      byte[] buffer = encode6and2 (xor);
      byte[] buffer2 = decode6and2 (buffer, 0);

      for (int i = 0; i < 256; i++)
        if (xor[i] != buffer2[i])
          System.out.println ("bollocks");

      return;
    }
  }

  public AddressField getAddressField (byte[] buffer, int offset)
  {
    return new AddressField (buffer, offset);
  }

  public DataField getDataField (byte[] buffer, int offset)
  {
    return new DataField (buffer, offset);
  }

  int decode4and4 (byte[] buffer, int offset)
  {
    int odds = ((buffer[offset] & 0xFF) << 1) + 1;
    int evens = buffer[offset + 1] & 0xFF;
    return odds & evens;
  }

  byte[] decode6and2 (byte[] buffer, int offset)
  {
    //    System.out.println ("\n\n343 byte disk buffer:\n");
    //    System.out.println (HexFormatter.format (buffer, offset, 343));

    byte[] temp = new byte[343];

    for (int i = 0; i < temp.length; i++)
    {
      int val = (buffer[offset++] & 0xFF) - 150;
      byte trans = readTranslateTable[val];
      assert trans != 0;
      temp[i] = (byte) ((trans - 1) << 2);
    }

    //    System.out.println ("\nTranslated 343 byte buffer:\n");
    //    System.out.println (HexFormatter.format (temp));

    byte[] temp2 = new byte[342];

    byte chk = 0;
    for (int i = 342; i > 0; i--)
    {
      temp2[i - 1] = (byte) (temp[i] ^ chk);
      chk = temp2[i - 1];
    }

    //    System.out.println ("\nChecksummed 342 byte buffer:\n");
    //    System.out.println (HexFormatter.format (temp2));
    //    System.out.printf ("%nChecksum: %02X%n", chk ^ temp2[0]);

    byte[] decodedBuffer = new byte[256];

    for (int i = 0; i < 256; i++)
      decodedBuffer[i] = temp2[i + 86];

    for (int i = 0; i < 84; i++)
    {
      int val = temp2[i] & 0xFF;
      int b1 = reverse ((val & 0x0C) >> 2);
      int b2 = reverse ((val & 0x30) >> 4);
      int b3 = reverse ((val & 0xC0) >> 6);

      decodedBuffer[i] |= b1;
      decodedBuffer[i + 86] |= b2;
      decodedBuffer[i + 172] |= b3;
    }

    for (int i = 84; i < 86; i++)
    {
      int val = temp2[i] & 0xFF;
      int b1 = reverse ((val & 0x0C) >> 2);
      int b2 = reverse ((val & 0x30) >> 4);

      decodedBuffer[i] |= b1;
      decodedBuffer[i + 86] |= b2;
    }

    //    System.out.println ("\nOriginal 256 byte buffer:\n");
    //    System.out.println (HexFormatter.format (decodedBuffer));

    return decodedBuffer;
  }

  byte[] encode6and2 (byte[] buffer)
  {
    //    System.out.println ("Original 256 byte buffer:\n");
    //    System.out.println (HexFormatter.format (buffer));

    byte[] temp1 = new byte[342];
    byte[] temp2 = new byte[343];
    byte[] temp3 = new byte[343];

    for (int i = 0; i < 256; i++)
      temp1[i + 86] = buffer[i];

    for (int i = 0; i < 84; i++)
    {
      int b1 = reverse (buffer[i] & 0x03) << 2;
      int b2 = reverse (buffer[i + 86] & 0x03) << 4;
      int b3 = reverse (buffer[i + 172] & 0x03) << 6;
      temp1[i] = (byte) (b1 | b2 | b3);
    }

    for (int i = 84; i < 86; i++)
    {
      int b1 = reverse (buffer[i] & 0x03) << 2;
      int b2 = reverse (buffer[i + 86] & 0x03) << 4;
      temp1[i] = (byte) (b1 | b2);
    }

    //    System.out.println ("\nNew 342 byte buffer:\n");
    //    System.out.println (HexFormatter.format (temp1));

    //    if (false)
    //    {
    //      temp2[0] = temp1[0];
    //      temp2[342] = temp1[341];
    //      for (int i = 1; i < 342; i++)
    //        temp2[i] = (byte) (temp1[i] ^ temp1[i - 1]);
    //    }
    //    else
    {
      byte chk = 0;
      for (int i = 0; i < 342; i++)
      {
        temp2[i] = (byte) (chk ^ temp1[i]);
        chk = temp1[i];
      }
      temp2[342] = chk;
    }

    //    System.out.println ("\nChecksummed 343 byte buffer:\n");
    //    System.out.println (HexFormatter.format (temp2));

    for (int i = 0; i < 343; i++)
      temp3[i] = writeTranslateTable[(temp2[i] & 0xFC) / 4];

    //    System.out.println ("\n\n\nTranslated 343 byte buffer:\n");
    //    System.out.println (HexFormatter.format (temp3));
    return temp3;
  }

  // reverse 2 bits - 0 <= b <= 3
  private static int reverse (int b)
  {
    return b == 1 ? 2 : b == 2 ? 1 : b;
  }

  private boolean matchBytes (byte[] buffer, int offset, byte[] valueBuffer)
  {
    for (int i = 0; i < valueBuffer.length; i++)
    {
      if (offset >= buffer.length)
        return false;
      if (buffer[offset++] != valueBuffer[i])
        return false;
    }
    return true;
  }

  int skipBytes (byte[] buffer, int offset, byte skipValue)
  {
    int count = 0;
    while (offset < buffer.length && buffer[offset++] == skipValue)
      ++count;
    return count;
  }

  int listBytes (byte[] buffer, int offset, int length)
  {
    int count = 0;
    for (int i = 0; i < length; i++)
    {
      if (offset >= buffer.length)
        break;
      System.out.printf ("%02X ", buffer[offset++]);
      ++count;
    }
    //    System.out.println ();
    return count;
  }

  abstract class Field
  {
    boolean valid;
    byte[] buffer;
    int offset;

    public Field (byte[] buffer, int offset)
    {
      this.buffer = buffer;
      this.offset = offset;
    }

    public boolean isValid ()
    {
      return valid;
    }

    public abstract int size ();
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
      {
        listBytes (buffer, offset, 14);
        System.out.println ();
      }
    }

    @Override
    public int size ()
    {
      return 14;
    }
  }

  class DataField extends Field
  {
    public DataField (byte[] buffer, int offset)
    {
      super (buffer, offset);

      if (matchBytes (buffer, offset, dataPrologue))
      {
        valid = true;
        if (!matchBytes (buffer, offset + 346, epilogue))
        {
          System.out.print ("   bad data epilogue: ");
          listBytes (buffer, offset + 346, 3);
          System.out.println ();
        }
      }
      else
      {
        System.out.print ("   bad data prologue: ");
        listBytes (buffer, offset, 3);
        System.out.println ();
      }
    }

    @Override
    public int size ()
    {
      return 349;
    }
  }
}