package com.bytezone.diskbrowser.disk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.bytezone.diskbrowser.utilities.Utility;

public class WozDisk
{
  private static final int TRK_SIZE = 0x1A00;
  private static final int INFO_SIZE = 0x3C;
  private static final int TMAP_SIZE = 0xA0;
  private static final int DATA_SIZE = TRK_SIZE - 10;
  private static byte[] header =
      { 0x57, 0x4F, 0x5A, 0x31, (byte) 0xFF, 0x0a, 0x0D, 0x0A };
  private static final String SPACES = "                                                ";
  private final boolean debug = false;

  final File file;
  final byte[] diskBuffer = new byte[4096 * 35];

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public WozDisk (File f)
  {
    this.file = f;
    Nibblizer nibbler = new Nibblizer (f);
    byte[] buffer = null;

    try
    {
      BufferedInputStream in = new BufferedInputStream (new FileInputStream (file));
      buffer = in.readAllBytes ();
      in.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      return;
    }

    assert matches (header, buffer);

    int cs1 = readInt (buffer, 8, 4);
    int cs2 = Utility.crc32 (buffer, 12, 256 - 12 + 35 * 6656);
    if (cs1 != cs2)
    {
      System.out.printf ("Checksum: %08X%n", cs1);
      System.out.printf ("Calculat: %08X%n", cs2);
    }

    int ptr = 12;
    read: while (ptr < buffer.length)
    {
      String chunkId = readString (buffer, ptr, 4);
      ptr += 4;
      int chunkSize = readInt (buffer, ptr, 4);
      ptr += 4;

      if ("INFO".equals (chunkId))
      {
        if (debug)
        {
          System.out.printf ("Version ........... %02X%n", buffer[ptr]);
          System.out.printf ("Disk type ......... %02X%n", buffer[ptr + 1]);
          System.out.printf ("Write protected ... %02X%n", buffer[ptr + 2]);
          System.out.printf ("Synchronised ...... %02X%n", buffer[ptr + 3]);
          System.out.printf ("Cleaned ........... %02X%n", buffer[ptr + 4]);
          System.out.printf ("Creator ........... %s%n%n",
              new String (buffer, ptr + 5, 32));
        }
        ptr += INFO_SIZE;
      }
      else if ("TMAP".equals (chunkId))
      {
        if (debug)
        {
          for (int track = 0; track < 40; track++)
          {
            for (int qtr = 0; qtr < 4; qtr++)
              System.out.printf ("%02X ", buffer[ptr++]);
            System.out.println ();
          }
          System.out.println ();
        }
        else
          ptr += TMAP_SIZE;
      }
      else if ("TRKS".equals (chunkId))
      {
        int tracks = chunkSize / TRK_SIZE;
        for (int track = 0; track < tracks; track++)
        {
          int bytesUsed = readInt (buffer, ptr + DATA_SIZE, 2);
          int bitCount = readInt (buffer, ptr + DATA_SIZE + 2, 2);

          byte[] trackData = new byte[bytesUsed];
          readTrack (buffer, ptr, trackData, bytesUsed);
          if (!nibbler.processTrack (track, trackData, diskBuffer))
          {
            System.out.println ("Nibblizer failure");
            break read;
          }
          ptr += TRK_SIZE;
        }
      }
      else if ("META".equals (chunkId))
      {
        System.out.printf ("[%s]  %08X%n", chunkId, chunkSize);
        ptr += chunkSize;
      }
      else
      {
        System.out.printf ("Unknown %08X%n", chunkSize);
        ptr += chunkSize;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // skip
  // ---------------------------------------------------------------------------------//

  private void skip (BufferedInputStream file, int size) throws IOException
  {
    while ((size -= file.skip (size)) > 0)
      ;
  }

  // ---------------------------------------------------------------------------------//
  // readString
  // ---------------------------------------------------------------------------------//

  private String readString (byte[] buffer, int offset, int length)
  {
    //    byte[] bytes = new byte[size];
    //    file.read (bytes);
    return new String (buffer, offset, length);
  }

  // ---------------------------------------------------------------------------------//
  // readInt
  // ---------------------------------------------------------------------------------//

  private int readInt (byte[] buffer, int offset, int length)
  {
    int shift = 0;
    int value = 0;
    for (int i = 0; i < length; i++)
    {
      value |= (buffer[offset + i] & 0xFF) << shift;
      shift += 8;
    }
    return value;
  }

  // ---------------------------------------------------------------------------------//
  // readInt
  // ---------------------------------------------------------------------------------//

  //  private int readInt (BufferedInputStream file, int size) throws IOException
  //  {
  //    byte[] buffer = new byte[size];
  //    file.read (buffer);
  //    return readInt (buffer, 0, size);
  //  }

  // ---------------------------------------------------------------------------------//
  // matches
  // ---------------------------------------------------------------------------------//

  private boolean matches (byte[] b1, byte[] b2)
  {
    for (int i = 0; i < b1.length; i++)
      if (b1[i] != b2[i])
        return false;
    return true;
  }

  // ---------------------------------------------------------------------------------//
  // readTrack
  // ---------------------------------------------------------------------------------//

  private void readTrack (byte[] buffer, int offset, byte[] trackData, int bytesUsed)
  {
    //    int consecutiveZeros = 0;
    int value = 0;
    int ptr = 0;

    for (int i = offset; i < offset + bytesUsed; i++)
    {
      int b = buffer[i] & 0xFF;
      for (int mask = 0x80; mask > 0; mask >>>= 1)
      {
        int bit = (b & mask) == 0 ? 0 : 1;

        value <<= 1;
        value |= bit;

        if ((value & 0x80) != 0)            // is hi-bit set?
        {
          trackData[ptr++] = (byte) (value & 0xFF);
          value = 0;
        }
      }
    }

    assert value == 0;
  }
}
