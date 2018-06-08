package com.bytezone.diskbrowser.disk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WozDisk
{
  private static final int TRK_SIZE = 0x1A00;
  private static final int DATA_SIZE = TRK_SIZE - 10;
  private static byte[] header =
      { 0x57, 0x4F, 0x5A, 0x31, (byte) 0xFF, 0x0a, 0x0D, 0x0A };
  private static final String SPACES = "                                                ";
  private final boolean debug = false;

  final File file;
  private final Nibblizer nibbler;
  final byte[] diskBuffer = new byte[4096 * 35];

  public WozDisk (File f)
  {
    this.file = f;
    nibbler = new Nibblizer (f);

    byte[] id = new byte[8];
    byte[] checksum = new byte[4];
    byte[] trackBuffer = new byte[TRK_SIZE];
    byte[] infoBuffer = new byte[60];
    byte[] tmapBuffer = new byte[160];

    try
    {
      BufferedInputStream in = new BufferedInputStream (new FileInputStream (file));
      in.read (id);
      assert matches (id, header);
      in.read (checksum);

      read: while (in.available () > 8)
      {
        String chunkId = readString (in, 4);
        int chunkSize = readInt (in, 4);

        if ("INFO".equals (chunkId))
        {
          if (debug)
          {
            in.read (infoBuffer);
            System.out.printf ("Version ........... %02X%n", infoBuffer[0]);
            System.out.printf ("Disk type ......... %02X%n", infoBuffer[1]);
            System.out.printf ("Write protected ... %02X%n", infoBuffer[2]);
            System.out.printf ("Synchronised ...... %02X%n", infoBuffer[3]);
            System.out.printf ("Cleaned ........... %02X%n", infoBuffer[4]);
            System.out.printf ("Creator ........... %s%n%n",
                new String (infoBuffer, 5, 32));
          }
          else
            in.skip (infoBuffer.length);
        }
        else if ("TMAP".equals (chunkId))
        {
          if (debug)
          {
            in.read (tmapBuffer);
            int ptr = 0;
            for (int track = 0; track < 40; track++)
            {
              for (int qtr = 0; qtr < 4; qtr++)
                System.out.printf ("%02X ", tmapBuffer[ptr++]);
              System.out.println ();
            }
            System.out.println ();
          }
          else
            in.skip (tmapBuffer.length);
        }
        else if ("TRKS".equals (chunkId))
        {
          int tracks = chunkSize / TRK_SIZE;
          for (int track = 0; track < tracks; track++)
          {
            int bytesRead = in.read (trackBuffer);
            assert bytesRead == TRK_SIZE;
            int bytesUsed = readInt (trackBuffer, DATA_SIZE, 2);
            int bitCount = readInt (trackBuffer, DATA_SIZE + 2, 2);

            byte[] trackData = new byte[bytesUsed];
            readTrack (trackBuffer, trackData, bytesUsed);
            if (!nibbler.processTrack (track, trackData, diskBuffer))
            {
              System.out.println ("Nibblizer failure");
              //              System.out.println (HexFormatter.format (trackBuffer, 0, trackBuffer.length,
              //                  TRK_SIZE * track + 256));
              //              System.out.println ();
              break read;
            }
          }
        }
        else if ("META".equals (chunkId))
        {
          System.out.printf ("[%s]  %08X%n", chunkId, chunkSize);
          skip (in, chunkSize);
        }
        else
        {
          System.out.printf ("Unknown %08X%n", chunkSize);
          skip (in, chunkSize);
        }
      }
      in.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      System.exit (1);
    }
  }

  private void skip (BufferedInputStream file, int size) throws IOException
  {
    while ((size -= file.skip (size)) > 0)
      ;
  }

  private String readString (BufferedInputStream file, int size) throws IOException
  {
    byte[] bytes = new byte[size];
    file.read (bytes);
    //    for (byte b : bytes)
    //      System.out.printf ("%02X ", b);
    //    System.out.println ();
    return new String (bytes);
  }

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

  private int readInt (BufferedInputStream file, int size) throws IOException
  {
    byte[] buffer = new byte[size];
    file.read (buffer);
    return readInt (buffer, 0, size);
  }

  private boolean matches (byte[] b1, byte[] b2)
  {
    for (int i = 0; i < b1.length; i++)
      if (b1[i] != b2[i])
        return false;
    return true;
  }

  private void readTrack (byte[] buffer, byte[] trackData, int bytesUsed)
  {
    //    int consecutiveZeros = 0;
    int value = 0;
    int ptr = 0;

    for (int i = 0; i < bytesUsed; i++)
    {
      int b = buffer[i] & 0xFF;
      for (int mask = 0x80; mask > 0; mask >>>= 1)
      {
        int bit = (b & mask) == 0 ? 0 : 1;
        //        if (bit == 1)
        //          consecutiveZeros = 0;
        //        else if (++consecutiveZeros > 3)
        //          bit = (random.nextInt () & 0x01);

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
