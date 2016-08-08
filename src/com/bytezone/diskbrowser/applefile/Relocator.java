package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class Relocator extends AbstractFile
{
  private final int checkByte;
  private final List<DiskRecord> diskRecords = new ArrayList<DiskRecord> ();
  private final List<MultiDiskAddress> addresses = new ArrayList<MultiDiskAddress> ();

  private final List<MultiDiskAddress> newAddresses = new ArrayList<MultiDiskAddress> ();
  private final List<MultiDiskAddress> oldAddresses = new ArrayList<MultiDiskAddress> ();

  private final List<MultiDiskAddress> logicalAddresses =
      new ArrayList<MultiDiskAddress> ();
  private final byte[] diskBlocks = new byte[0x800];
  private final int[] diskOffsets = new int[0x800];
  private final AppleDisk[] disks = new AppleDisk[5];

  public Relocator (String name, byte[] buffer)
  {
    super (name, buffer);

    checkByte = HexFormatter.intValue (buffer[0], buffer[1]);

    int ptr = 2;            // skip checkByte

    while (buffer[ptr] != 0)
    {
      DiskRecord diskRecord = new DiskRecord (buffer, ptr);
      diskRecords.add (diskRecord);
      ptr += diskRecord.size ();
    }

    logicalAddresses.add (new MultiDiskAddress (0, 0, 0, 0x800));

    for (DiskRecord diskRecord : diskRecords)
      for (DiskSegment diskSegment : diskRecord.diskSegments)
      {
        addresses
            .add (new MultiDiskAddress (diskRecord.diskNumber, diskSegment.logicalBlock,
                diskSegment.physicalBlock, diskSegment.segmentLength));
        addLogicalBlock ((byte) diskRecord.diskNumber, diskSegment);
      }

    getMultiDiskAddress ("BOOT", 0, 2);
    getMultiDiskAddress ("CATALOG", 2, 4);
  }

  public void list ()
  {
    for (MultiDiskAddress multiDiskAddress : addresses)
      System.out.printf ("%d  %03X  %03X  %03X  %s%n", multiDiskAddress.diskNumber,
          multiDiskAddress.logicalBlockNumber, multiDiskAddress.physicalBlockNumber,
          multiDiskAddress.totalBlocks, multiDiskAddress.name);
  }

  private void addLogicalBlock (byte disk, DiskSegment diskSegment)
  {
    int lo = diskSegment.logicalBlock;
    int hi = diskSegment.logicalBlock + diskSegment.segmentLength;
    for (int i = lo; i < hi; i++)
      if (diskBlocks[i] == 0)
      {
        diskBlocks[i] = disk;
        diskOffsets[i] = diskSegment.physicalBlock;
      }
  }

  public List<MultiDiskAddress> getMultiDiskAddress (String name, int blockNumber,
      int length)
  {
    List<MultiDiskAddress> foundAddresses = new ArrayList<MultiDiskAddress> ();
    newAddresses.clear ();
    oldAddresses.clear ();
    //    System.out.printf ("%04X  %04X  %s%n", blockNumber, length, name);

    for (MultiDiskAddress multiDiskAddress : addresses)
    {
      if (multiDiskAddress.logicalBlockNumber == blockNumber)
      {
        if (multiDiskAddress.totalBlocks == length)
        {
          foundAddresses.add (multiDiskAddress);
          if (multiDiskAddress.name.isEmpty ())
            multiDiskAddress.name = name;
        }
        else if (multiDiskAddress.totalBlocks > length)
        {
          MultiDiskAddress newAddress1 = new MultiDiskAddress (
              multiDiskAddress.diskNumber, multiDiskAddress.logicalBlockNumber,
              multiDiskAddress.physicalBlockNumber, length, name);
          MultiDiskAddress newAddress2 = new MultiDiskAddress (
              multiDiskAddress.diskNumber, multiDiskAddress.logicalBlockNumber + length,
              multiDiskAddress.physicalBlockNumber + length,
              multiDiskAddress.totalBlocks - length);
          oldAddresses.add (multiDiskAddress);
          newAddresses.add (newAddress1);
          newAddresses.add (newAddress2);
          foundAddresses.add (newAddress1);
        }
      }
    }

    if (newAddresses.size () > 0)
    {
      addresses.addAll (newAddresses);
      addresses.removeAll (oldAddresses);
      Collections.sort (addresses);
    }

    return foundAddresses;
  }

  public void addDisk (AppleDisk disk)
  {
    byte[] buffer = disk.readSector (1);
    int diskNo = buffer[510] & 0xFF;
    if (diskNo > 0 && diskNo <= 5)
      disks[diskNo - 1] = disk;
  }

  public boolean hasData ()
  {
    for (AppleDisk disk : disks)
      if (disk == null)
        return false;
    return true;
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Pascal Relocator\n\n");
    text.append (String.format ("Check byte..... %04X%n%n", checkByte));

    for (DiskRecord diskRecord : diskRecords)
    {
      text.append (diskRecord);
      text.append ("\n");
    }

    int previousDiskNumber = 0;
    for (MultiDiskAddress multiDiskAddress : addresses)
    {
      if (multiDiskAddress.diskNumber != previousDiskNumber)
      {
        previousDiskNumber = multiDiskAddress.diskNumber;
        text.append ("\n");
        text.append ("Disk  Logical  Physical   Size   Name\n");
        text.append ("----  -------  --------   ----   -------------\n");
      }
      text.append (String.format ("  %d     %03X       %03X      %03X   %s%n",
          multiDiskAddress.diskNumber, multiDiskAddress.logicalBlockNumber,
          multiDiskAddress.physicalBlockNumber, multiDiskAddress.totalBlocks,
          multiDiskAddress.name));
    }

    text.append ("\n\n Logical   Size  Disk  Physical");
    text.append ("\n---------  ----  ----  ---------\n");

    int first = 0;
    int lastDisk = diskBlocks[0];
    int lastOffset = diskOffsets[0];
    for (int i = 0; i < diskBlocks.length; i++)
    {
      if (diskBlocks[i] != lastDisk || diskOffsets[i] != lastOffset)
      {
        int size = i - first;
        if (lastDisk > 0)
          text.append (String.format ("%03X - %03X   %03X    %d   %03X - %03X%n", first,
              i - 1, size, lastDisk, lastOffset, lastOffset + size - 1));
        else
          text.append (String.format ("%03X - %03X   %03X%n", first, i - 1, size));
        first = i;
        lastDisk = diskBlocks[i];
        lastOffset = diskOffsets[i];
      }
    }

    if (lastDisk > 0)
    {
      int max = diskBlocks.length;
      int size = max - first;
      text.append (String.format ("%03X - %03X   %03X    %d   %03X - %03X%n", first,
          max - 1, size, lastDisk, lastOffset, lastOffset + size - 1));
    }

    return text.toString ();
  }

  private class DiskRecord
  {
    int diskNumber;
    int totDiskSegments;
    List<DiskSegment> diskSegments = new ArrayList<DiskSegment> ();

    public DiskRecord (byte[] buffer, int ptr)
    {
      diskNumber = HexFormatter.intValue (buffer[ptr], buffer[ptr + 1]);
      totDiskSegments = HexFormatter.intValue (buffer[ptr + 2], buffer[ptr + 4]);

      ptr += 4;
      for (int i = 0; i < totDiskSegments; i++)
      {
        diskSegments.add (new DiskSegment (buffer, ptr));
        ptr += 6;
      }
    }

    int size ()
    {
      return 4 + diskSegments.size () * 6;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Disk number.... %04X%n", diskNumber));
      text.append (String.format ("Segments....... %04X%n%n", totDiskSegments));
      text.append (String.format (" Seg   Skip   Size     Logical      Physical%n"));
      text.append (String.format (" ---   ----   ----   -----------   -----------%n"));

      int count = 1;
      int last = 0;
      int size = 0;

      for (DiskSegment segment : diskSegments)
      {
        if (segment.logicalBlock > last)
        {
          int end = segment.logicalBlock - 1;
          size = end - last + 1;
        }
        last = segment.logicalBlock + segment.segmentLength;
        text.append (
            String.format ("  %02X   %04X  %s %n", count++, size, segment.toString ()));
      }

      return text.toString ();
    }
  }

  private class DiskSegment
  {
    int logicalBlock;
    int physicalBlock;
    int segmentLength;

    public DiskSegment (byte[] buffer, int ptr)
    {
      logicalBlock = HexFormatter.intValue (buffer[ptr], buffer[ptr + 1]);
      physicalBlock = HexFormatter.intValue (buffer[ptr + 2], buffer[ptr + 3]);
      segmentLength = HexFormatter.intValue (buffer[ptr + 4], buffer[ptr + 5]);
    }

    @Override
    public String toString ()
    {
      return String.format (" %04X   %04X - %04X   %04X - %04X", segmentLength,
          logicalBlock, (logicalBlock + segmentLength - 1), physicalBlock,
          (physicalBlock + segmentLength - 1));
    }

    //    public String toString (int offset)
    //    {
    //      int logical = logicalBlock - offset;
    //      int physical = physicalBlock - offset;
    //      if (physical >= 0)
    //        return String.format ("    %04X      %04X      %04X      %04X      %04X",
    //            logicalBlock, physicalBlock, segmentLength, logical, physical);
    //      return String.format ("    %04X      %04X      %04X", logicalBlock, physicalBlock,
    //          segmentLength);
    //    }
  }

  class MultiDiskAddress implements Comparable<MultiDiskAddress>
  {
    int diskNumber;
    int logicalBlockNumber;
    int physicalBlockNumber;
    int totalBlocks;
    String name = "";

    public MultiDiskAddress (int diskNumber, int logicalBlockNumber,
        int physicalBlockNumber, int totalBlocks)
    {
      this.diskNumber = diskNumber;
      this.logicalBlockNumber = logicalBlockNumber;
      this.physicalBlockNumber = physicalBlockNumber;
      this.totalBlocks = totalBlocks;
    }

    public MultiDiskAddress (int diskNumber, int logicalBlockNumber,
        int physicalBlockNumber, int totalBlocks, String name)
    {
      this (diskNumber, logicalBlockNumber, physicalBlockNumber, totalBlocks);
      this.name = name;
    }

    @Override
    public String toString ()
    {
      return String.format ("%d:%03X", diskNumber, physicalBlockNumber);
    }

    @Override
    public int compareTo (MultiDiskAddress o)
    {
      if (this.diskNumber == o.diskNumber)
        return this.logicalBlockNumber - o.logicalBlockNumber;
      return this.diskNumber - o.diskNumber;
    }
  }
}