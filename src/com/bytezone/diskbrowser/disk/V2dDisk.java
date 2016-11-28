package com.bytezone.diskbrowser.disk;

import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

/*
 * Offsets in bytes
 * From To Meaning
 * 0000 0003 Length of disk image, from offset 8 to the end (big endian)
 * 0004 0007 Type indication; always 'D5NI'
 * 0008 0009 Number of tracks (typical value: $23, but can be different). Also big endian.
 * 000A 000B Track nummer * 4, starting with track # 0. Big endian.
 * 000C 000D Length of track, not counting this length-field. Big endian.
 * 000E 000E + length field - 1 Nibble-data of track, byte-for-byte.
 * After this, the pattern from offset 000A repeats for each track. Note that
 * "track number * 4" means that on a regular disk the tracks are numbered 0, 4, 8 etc.
 * Half-tracks are numbered 2, 6, etc. The stepper motor of the disk uses 4 phases to
 * travel from one track to the next, so quarter-tracks could also be stored with this
 * format (I have never heard of disk actually using quarter tracks, though).
 */
public class V2dDisk implements Disk
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

  private static byte[] readTranslateTable = new byte[106];

  static
  {
    for (int i = 0; i < writeTranslateTable.length; i++)
    {
      int j = (writeTranslateTable[i] & 0xFF) - 150;
      readTranslateTable[j] = (byte) (i + 1);
    }
  }

  public V2dDisk (File file)
  {
    //    byte[] buffer = encode62 (xor);
    //    byte[] buffer2 = decode62 (buffer, 0);
    //
    //    for (int i = 0; i < 256; i++)
    //      if (xor[i] != buffer2[i])
    //        System.out.println ("bollocks");

    //    if (true)
    //      return;

    byte[] diskBuffer = new byte[10];

    try
    {
      BufferedInputStream in = new BufferedInputStream (new FileInputStream (file));
      in.read (diskBuffer);

      int diskLength = HexFormatter.getLongBigEndian (diskBuffer, 0);   // 4 bytes
      System.out.printf ("Disk length: %,d%n", diskLength);
      String id = HexFormatter.getString (diskBuffer, 4, 4);            // 4 bytes
      System.out.printf ("ID: %s%n", id);
      int tracks = HexFormatter.getShortBigEndian (diskBuffer, 8);      // 2 bytes
      System.out.printf ("Tracks: %d%n", tracks);

      for (int i = 0; i < tracks; i++)
      {
        byte[] trackHeader = new byte[4];
        in.read (trackHeader);
        int trackNumber = HexFormatter.getShortBigEndian (trackHeader, 0);
        int trackLength = HexFormatter.getShortBigEndian (trackHeader, 2);

        int fullTrackNo = trackNumber / 4;
        int halfTrackNo = trackNumber % 4;

        byte[] trackData = new byte[trackLength];
        in.read (trackData);

        //        if (false)
        processTrack (trackData);
      }

      in.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      System.exit (1);
    }
  }

  private void processTrack (byte[] buffer)
  {
    int ptr = 0;
    int track, sector, volume, checksum;

    if (buffer[0] == (byte) 0xEB)
    {
      System.out.println ("overrun");
      ++ptr;
    }

    ptr += skipBytes (buffer, ptr, (byte) 0xFF);

    while (ptr < buffer.length)
    {
      if (matchBytes (buffer, ptr, addressPrologue)
          && matchBytes (buffer, ptr + 11, epilogue))
      {
        volume = decode44 (buffer, ptr + 3);
        track = decode44 (buffer, ptr + 5);
        sector = decode44 (buffer, ptr + 7);
        checksum = decode44 (buffer, ptr + 9);
        System.out.printf ("Volume: %03d, Track: %02d, Sector: %02d, Checksum: %03d%n",
            volume, track, sector, checksum);
        ptr += 14;
      }
      else
      {
        System.out.println ("Invalid address prologue/epilogue");
        ptr += listBytes (buffer, ptr, 14);
        break;
      }

      ptr += skipBytes (buffer, ptr, (byte) 0xFF);

      if (!matchBytes (buffer, ptr, dataPrologue))
      {
        System.out.println ("Invalid data prologue");
        ptr += listBytes (buffer, ptr, dataPrologue.length);
        break;
      }

      if (!matchBytes (buffer, ptr + 346, epilogue))
      {
        System.out.println ("Invalid data epilogue");
        ptr += listBytes (buffer, ptr + 346, epilogue.length);
        break;
      }

      ptr += dataPrologue.length;
      //      if (track == 0 && sector == 0)
      {
        byte[] decodedBuffer = decode62 (buffer, ptr);
        System.out.println (HexFormatter.format (decodedBuffer));
      }
      ptr += 342;
      ptr += 1;     // checksum
      ptr += epilogue.length;

      //      System.out.print ("<--- 342 data bytes --> ");

      //      ptr += listBytes (buffer, ptr, 4);
      ptr += skipBytes (buffer, ptr, (byte) 0xFF);
    }

    System.out.println ("----------------------------------------------");
  }

  private int skipBytes (byte[] buffer, int offset, byte skipValue)
  {
    int count = 0;
    while (offset < buffer.length && buffer[offset++] == skipValue)
      ++count;
    System.out.printf ("   (%2d x %02X)%n", count, skipValue);
    return count;
  }

  private int listBytes (byte[] buffer, int offset, int length)
  {
    int count = 0;
    for (int i = 0; i < length; i++)
    {
      if (offset >= buffer.length)
        break;
      System.out.printf ("%02X ", buffer[offset++]);
      ++count;
    }
    System.out.println ();
    return count;
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

  private int decode44 (byte[] buffer, int offset)
  {
    int odds = ((buffer[offset] & 0xFF) << 1) + 1;
    int evens = buffer[offset + 1] & 0xFF;
    return odds & evens;
  }

  private byte[] decode62 (byte[] buffer, int offset)
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

  private byte[] encode62 (byte[] buffer)
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

    temp2[0] = temp1[0];
    temp2[342] = temp1[341];
    for (int i = 1; i < 342; i++)
      temp2[i] = (byte) (temp1[i] ^ temp1[i - 1]);

    //    System.out.println ("\nChecksummed 343 byte buffer:\n");
    //    System.out.println (HexFormatter.format (temp2));

    for (int i = 0; i < 343; i++)
      temp3[i] = writeTranslateTable[(temp2[i] & 0xFC) / 4];

    //    System.out.println ("\n\n\nTranslated 343 byte buffer:\n");
    //    System.out.println (HexFormatter.format (temp3));
    return temp3;
  }

  private static int reverse (int b)
  {
    if (b == 1)
      return 2;
    if (b == 2)
      return 1;
    return b;
  }

  @Override
  public Iterator<DiskAddress> iterator ()
  {
    return null;
  }

  @Override
  public long getBootChecksum ()
  {
    return 0;
  }

  @Override
  public void setEmptyByte (byte value)
  {
  }

  @Override
  public int getTotalBlocks ()
  {
    return 0;
  }

  @Override
  public int getTotalTracks ()
  {
    return 0;
  }

  @Override
  public int getBlockSize ()
  {
    return 0;
  }

  @Override
  public void setBlockSize (int blockSize)
  {
  }

  @Override
  public int getTrackSize ()
  {
    return 0;
  }

  @Override
  public int getSectorsPerTrack ()
  {
    return 0;
  }

  @Override
  public void setInterleave (int interleave)
  {
  }

  @Override
  public int getInterleave ()
  {
    return 0;
  }

  @Override
  public DiskAddress getDiskAddress (int block)
  {
    return null;
  }

  @Override
  public List<DiskAddress> getDiskAddressList (int... blocks)
  {
    return null;
  }

  @Override
  public DiskAddress getDiskAddress (int track, int sector)
  {
    return null;
  }

  @Override
  public byte[] readSector (int block)
  {
    return null;
  }

  @Override
  public byte[] readSector (int track, int sector)
  {
    return null;
  }

  @Override
  public byte[] readSector (DiskAddress da)
  {
    return null;
  }

  @Override
  public byte[] readSectors (List<DiskAddress> daList)
  {
    return null;
  }

  @Override
  public void writeSector (DiskAddress da, byte[] buffer)
  {
  }

  @Override
  public boolean isSectorEmpty (DiskAddress da)
  {
    return false;
  }

  @Override
  public boolean isSectorEmpty (int block)
  {
    return false;
  }

  @Override
  public boolean isSectorEmpty (int track, int sector)
  {
    return false;
  }

  @Override
  public boolean isValidAddress (int block)
  {
    return false;
  }

  @Override
  public boolean isValidAddress (int track, int sector)
  {
    return false;
  }

  @Override
  public boolean isValidAddress (DiskAddress da)
  {
    return false;
  }

  @Override
  public File getFile ()
  {
    return null;
  }

  @Override
  public void addActionListener (ActionListener listener)
  {
  }

  @Override
  public void removeActionListener (ActionListener listener)
  {
  }

}