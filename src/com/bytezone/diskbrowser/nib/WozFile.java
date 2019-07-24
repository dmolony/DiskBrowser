package com.bytezone.diskbrowser.nib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

public class WozFile
{
  private static final byte[] WOZ1_FILE_HEADER =
      { 0x57, 0x4F, 0x5A, 0x31, (byte) 0xFF, 0x0A, 0x0D, 0x0A };
  private static final byte[] WOZ2_FILE_HEADER =
      { 0x57, 0x4F, 0x5A, 0x32, (byte) 0xFF, 0x0A, 0x0D, 0x0A };

  private static final int TRK_SIZE = 0x1A00;
  private static final int INFO_SIZE = 0x3C;
  private static final int TMAP_SIZE = 0xA0;
  private static final int DATA_SIZE = TRK_SIZE - 10;

  private final boolean debug = true;
  private final boolean dump = true;
  private int diskType;                       // 5.25 or 3.5
  private int wozVersion;
  private int bootSectorFormat;

  public final File file;
  byte[] diskBuffer;

  private final MC3470 mc3470 = new MC3470 ();
  private final List<NibbleTrack> nibbleTracks = new ArrayList<> (40);

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  public WozFile (File file) throws DiskNibbleException
  {
    this.file = file;
    byte[] buffer = readFile ();
    boolean valid = false;

    if (matches (WOZ1_FILE_HEADER, buffer))
      wozVersion = 1;
    else if (matches (WOZ2_FILE_HEADER, buffer))
      wozVersion = 2;
    else
    {
      System.out.println (HexFormatter.format (buffer, 0, 20));
      throw new DiskNibbleException ("Header error");
    }

    int checksum1 = readInt (buffer, 8, 4);
    int checksum2 = Utility.crc32 (buffer, 12, buffer.length - 12);
    if (checksum1 != checksum2)
    {
      System.out.printf ("Stored checksum     : %08X%n", checksum1);
      System.out.printf ("Calculated checksum : %08X%n", checksum2);
      throw new DiskNibbleException ("Checksum error");
    }

    int ptr = 12;
    read: while (ptr < buffer.length)
    {
      String chunkId = new String (buffer, ptr, 4);
      ptr += 4;
      int chunkSize = readInt (buffer, ptr, 4);
      ptr += 4;

      if (debug)
      {
        System.out.printf ("Offset    : %06X%n", ptr - 8);
        System.out.printf ("Chunk ID  : %s%n", chunkId);
        System.out.printf ("Chunk size: %,d%n", chunkSize);
      }

      if ("INFO".equals (chunkId))
      {
        if (debug)
        {
          int diskType = buffer[ptr + 1] & 0xFF;
          String diskTypeText = diskType == 1 ? "5.25" : diskType == 2 ? "3.5" : "??";

          System.out.println ();
          System.out.printf ("Version ........... %02X%n", buffer[ptr]);
          System.out.printf ("Disk type ......... %02X  %s%n", diskType, diskTypeText);
          System.out.printf ("Write protected ... %02X%n", buffer[ptr + 2]);
          System.out.printf ("Synchronised ...... %02X%n", buffer[ptr + 3]);
          System.out.printf ("Cleaned ........... %02X%n", buffer[ptr + 4]);
          System.out.printf ("Creator ........... %s%n",
              new String (buffer, ptr + 5, 32).trim ());

          if (wozVersion > 1)
          {
            int bootFormat = buffer[ptr + 38] & 0xFF;
            String bootFormatText = bootFormat == 1 ? "16 sector"
                : bootFormat == 2 ? "13 sector" : bootFormat == 3 ? "Both" : "??";
            System.out.printf ("Disk sides ........ %02X%n", buffer[ptr + 37]);
            System.out.printf ("Boot format ....... %02X  %s%n", bootFormat,
                bootFormatText);
            System.out.printf ("Optimal timing .... %02X%n", buffer[ptr + 39]);
            System.out.printf ("Compatible flags .. %04X%n",
                readInt (buffer, ptr + 40, 2));
            System.out.printf ("Minimum RAM ....... %04X%n",
                readInt (buffer, ptr + 42, 2));
            System.out.printf ("Largest track ..... %04X%n",
                readInt (buffer, ptr + 44, 2));
          }
          System.out.println ();
        }

        diskType = buffer[ptr + 1] & 0xFF;
        if (wozVersion > 1)
          bootSectorFormat = buffer[ptr + 38] & 0xFF;

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
        if (debug)
        {
          System.out.println ("***********************************************");
          System.out.printf ("*  Disk ......... %s%n", file.getName ());
          System.out.println ("***********************************************");
        }

        if (wozVersion == 1)
        {
          int tracks = chunkSize / TRK_SIZE;

          for (int trackNo = 0; trackNo < tracks; trackNo++)
          {
            int bytesUsed = readInt (buffer, ptr + DATA_SIZE, 2);
            int bitCount = readInt (buffer, ptr + DATA_SIZE + 2, 2);

            if (debug)
            {
              System.out.println ("***************************************");
              System.out.printf ("*   Track ......... %,6d of %,6d  *%n", trackNo,
                  tracks);
              System.out.printf ("*   Bytes used .... %,6d            *%n", bytesUsed);
              System.out.printf ("*   Bit count  .... %,6d            *%n", bitCount);
              System.out.println ("***************************************");
            }

            try
            {
              // nibbleTracks.add (mc3470.getNibbleTrack (buffer, ptr, bytesUsed, bitCount));
              List<RawDiskSector> diskSectors =
                  mc3470.readTrack (buffer, ptr, bytesUsed, bitCount);

              if (trackNo == 0)         // create disk buffer
              {
                if (mc3470.is13Sector ())
                  diskBuffer = new byte[35 * 13 * 256];
                else if (mc3470.is16Sector ())
                  diskBuffer = new byte[35 * 16 * 256];
                else
                {
                  System.out.println ("unknown disk format");
                  break read;
                }
              }

              mc3470.storeSectors (diskSectors, diskBuffer);
            }
            catch (Exception e)
            {
              System.out.println (e);
              break read;
            }

            ptr += TRK_SIZE;
          }
        }
        else
        {
          diskBuffer = new byte[(bootSectorFormat == 2 ? 13 : 16) * 35 * 256];

          for (int trackNo = 0; trackNo < 160; trackNo++)
          {
            int p = 256 + trackNo * 8;
            int startingBlock = readInt (buffer, p, 2);
            int blockCount = readInt (buffer, p + 2, 2);
            int bitCount = readInt (buffer, p + 4, 4);

            if (debug)
            {
              System.out.println ("******************************");
              System.out.printf ("*   Track ......... %,6d   *%n", trackNo);
              System.out.printf ("*   Start block ... %,6d   *%n", startingBlock);
              System.out.printf ("*   Block count ... %,6d   *%n", blockCount);
              System.out.printf ("*   Bit count  .... %,6d   *%n", bitCount);
              System.out.println ("******************************");
            }

            if (startingBlock == 0)
              break;

            try
            {
              // nibbleTracks.add (mc3470.getNibbleTrack (buffer, ptr, bytesUsed, bitCount));
              List<RawDiskSector> diskSectors = mc3470.readTrack (buffer,
                  startingBlock * 512, blockCount * 512, bitCount);

              for (RawDiskSector rawDiskSector : diskSectors)
              {
                System.out.println (rawDiskSector);
                rawDiskSector.dump ();
              }

              mc3470.storeSectors (diskSectors, diskBuffer);
            }
            catch (Exception e)
            {
              System.out.println (e);
              break read;
            }
          }
          ptr += chunkSize;
        }
      }
      else if ("META".equals (chunkId))
      {
        //        System.out.printf ("[%s]  %08X%n", chunkId, chunkSize);
        //        System.out.println (HexFormatter.format (buffer, ptr, chunkSize));
        ptr += chunkSize;
      }
      else
      {
        System.out.printf ("Unknown %08X%n", chunkSize);
        ptr += chunkSize;
      }
    }

    //    if (!valid)
    //      readNibbleTracks (buffer);
  }

  // ---------------------------------------------------------------------------------//
  // readNibbleTracks
  // ---------------------------------------------------------------------------------//

  private void readNibbleTracks (byte[] buffer)
  {
    for (int track = 0; track < 35; track++)
    {
      int ptr = track * 6656 + 256;

      int bytesUsed = readInt (buffer, ptr + DATA_SIZE, 2);
      int bitCount = readInt (buffer, ptr + DATA_SIZE + 2, 2);

      NibbleTrack nibbleTrack = mc3470.getNibbleTrack (buffer, ptr, bytesUsed, bitCount);
      nibbleTracks.add (nibbleTrack);
    }
  }

  // ---------------------------------------------------------------------------------//
  // getSectorsPerTrack
  // ---------------------------------------------------------------------------------//

  public int getSectorsPerTrack ()
  {
    return mc3470.is13Sector () ? 13 : mc3470.is16Sector () ? 16 : 0;
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
  // getDiskBuffer
  // ---------------------------------------------------------------------------------//

  public byte[] getDiskBuffer ()
  {
    return diskBuffer;
  }

  // ---------------------------------------------------------------------------------//
  // readFile
  // ---------------------------------------------------------------------------------//

  private byte[] readFile ()
  {
    try
    {
      BufferedInputStream in = new BufferedInputStream (new FileInputStream (file));
      byte[] buffer = in.readAllBytes ();
      in.close ();
      return buffer;
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      return null;
    }
  }

  void dump (int trackNo)
  {

  }

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

  public static void main (String[] args)
  {
    File file = new File ("/Users/denismolony/code/python/wozardry-2.0/bill.woz");
    try
    {
      WozFile wozFile = new WozFile (file);
    }
    catch (DiskNibbleException e)
    {
      e.printStackTrace ();
    }
  }
}
