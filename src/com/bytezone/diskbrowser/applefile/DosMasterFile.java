package com.bytezone.diskbrowser.applefile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.prodos.ProdosDisk;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class DosMasterFile extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private static final String base = "/Users/denismolony/Dropbox/Examples/Testing/";

  // ---------------------------------------------------------------------------------//
  public DosMasterFile (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isDos33 (ProdosDisk parentDisk, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    //    System.out.println (HexFormatter.format (buffer, 0x38, 0x30, 0x38));
    System.out.printf ("%nHighest Block: %04X (%<,d)%n",
        parentDisk.getDisk ().getTotalBlocks () - 1);

    System.out.print ("\nSlots/Drives: ");
    for (int i = 0; i < 8; i++)
    {
      System.out.printf ("%02X    ", buffer[0x38 + i]);
      if (i % 2 == 1)
        System.out.print (": ");
    }

    System.out.print ("\nFirst Block : ");
    for (int i = 0; i < 16; i += 2)
    {
      System.out.printf ("%04X  ", Utility.unsignedShort (buffer, 0x40 + i));
      if (i % 4 == 2)
        System.out.print (": ");
    }

    System.out.print ("\nLast Block  : ");
    for (int i = 0; i < 8; i += 2)
      System.out.printf ("%04X        : ", Utility.unsignedShort (buffer, 0x50 + i));

    System.out.print ("\nImage Size  : ");
    for (int i = 0; i < 8; i += 2)
      System.out.printf ("%04X        : ", Utility.unsignedShort (buffer, 0x58 + i));

    System.out.print ("\nAddress     : ");
    for (int i = 0; i < 8; i += 2)
      System.out.printf ("%04X        : ", Utility.unsignedShort (buffer, 0x60 + i));

    System.out.println ();
    System.out.println ();
    System.out.println ("#      S  D  B Lo  B Hi  Size  Vols  Secs");

    Disk disk = parentDisk.getDisk ();

    for (int i = 0; i < 8; i++)
    {
      int slotDrive = buffer[0x38 + i] & 0xFF;
      if (slotDrive == 0)
        continue;

      int slot = (slotDrive & 0x70) >>> 4;
      int drive = ((slotDrive & 0x80) >>> 7) + 1;
      int firstBlock = Utility.unsignedShort (buffer, 0x40 + i * 2);      // of first volume

      int skip = i / 2 * 2;      // 0, 0, 2, 2, 4, 4, 6, 6 - same for both drives

      int lastBlock = Utility.unsignedShort (buffer, 0x50 + skip);        // of last volume
      int volSize = Utility.unsignedShort (buffer, 0x58 + skip);

      int originalFirstBlock = firstBlock;
      if (firstBlock > lastBlock)        // WTF?
        firstBlock -= 0x10000;

      int vols = (lastBlock - firstBlock) / volSize - 1;
      int sectors = volSize * 2;

      System.out.printf ("%d  %02X  %d  %d  %04X  %04X  %04X   %3d  %4d%n", i,
          buffer[0x38 + i], slot, drive, originalFirstBlock, lastBlock, volSize, vols,
          sectors);

      if (vols > 0 && true)
      {
        int volNo = 1;

        int firstDiskBlock = firstBlock + volNo * volSize;
        int lastDiskBlock = firstDiskBlock + volSize;

        List<DiskAddress> daList = new ArrayList<> ();
        for (int block = firstDiskBlock; block < lastDiskBlock; block++)
          daList.add (disk.getDiskAddress (block));

        byte[] diskBuffer = disk.readBlocks (daList);
        //        System.out.println (HexFormatter.format (diskBuffer));
        //        System.out.printf ("Buffer: %,d%n", diskBuffer.length);
        //        System.out.printf ("Blocks: %,d x 2 = %,d%n", daList.size (), daList.size () * 2);

        if (false)
          createDisk (String.format ("%sVol%03d.dsk", base, volNo), diskBuffer);
      }
    }

    //    oldCode (parentDisk, buffer);

    return false;
  }

  // ---------------------------------------------------------------------------------//
  private static void createDisk (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    File file = new File (name);
    if (file.exists ())
    {
      System.out.printf ("File: %s already exists%n", name);
      return;
    }

    try
    {
      Path path = Paths.get (name);
      Files.write (path, buffer);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private static void oldCode (ProdosDisk parentDisk, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {

    int slots = 3 * 16 + 8;
    int v0 = slots + 8;
    int size = v0 + 16;
    int vsiz = size + 8;
    //    int adrs = vsiz + 8;

    System.out.println ();
    System.out.println (
        "Slots  v0   size  vsiz    d    d0   s/d   ptr   strt     sz      end    vols");

    StringBuilder text = new StringBuilder ();

    for (int d = 0; d < 8; d++)
    {
      int d0 = d / 2 * 2;
      int s = buffer[slots + d] & 0xFF;

      if (s == 0)
        continue;

      System.out.printf (" %02X    %02X    %02X    %02X    %02X    %02X    %02X", slots,
          v0, size, vsiz, d, d0, s);

      int dr = 0;
      if (s > 127)
      {
        s -= 128;
        dr = 1;
      }

      text.append (String.format ("Slot %d, Drive %d has", s / 16, dr + 1));

      int ptr = v0 + 2 * d0 + 2 * dr;
      int st = Utility.unsignedShort (buffer, ptr);             // start block of first volume
      int v = Utility.unsignedShort (buffer, size + d0);        // end block of last volume
      int sz = Utility.unsignedShort (buffer, vsiz + d0);       // blocks per volume

      if (st > v)
        st -= 16 * 4096;

      int num = (v - st) / sz - 1;

      text.append (String.format (" %d volumes of %d sectors%n", num, sz * 2));

      System.out.printf ("   %02X    %04X    %04X    %04X    %04X%n", ptr, st, sz, v,
          num);

      Disk disk = parentDisk.getDisk ();

      if (num > 0 && false)
      {
        for (int i = 1; i <= num; i++)
        //        int i = 15;
        {
          int firstBlock = st + i * sz;
          int lastBlock = firstBlock + sz;
          int length = lastBlock - firstBlock;
          System.out.printf ("%3d  %04X  %04X  %04X%n", i, firstBlock, lastBlock, length);

          List<DiskAddress> daList = new ArrayList<> ();
          for (int block = firstBlock; block < lastBlock; block++)
            daList.add (disk.getDiskAddress (block));
          byte[] diskBuffer = disk.readBlocks (daList);

          if (false)
            createDisk (String.format ("%sVol%03d.dsk", base, i), diskBuffer);
        }
      }
    }
    System.out.println ();
    System.out.println (text.toString ());
  }
}
