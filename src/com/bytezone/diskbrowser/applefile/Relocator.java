package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class Relocator extends AbstractFile
{
  private final int checkByte;
  private final List<DiskRecord> diskRecords = new ArrayList<DiskRecord> ();

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
  }

  public List<MultiDiskAddress> getMultiDiskAddress (int blockNumber)
  {
    System.out.printf ("searching for %04X%n", blockNumber);
    List<MultiDiskAddress> addresses = new ArrayList<MultiDiskAddress> ();

    for (DiskRecord diskRecord : diskRecords)
      for (DiskSegment diskSegment : diskRecord.diskSegments)
        if (diskSegment.logicalBlock == blockNumber)
          addresses.add (new MultiDiskAddress (diskRecord.diskNumber,
              diskSegment.physicalBlock, diskSegment.segmentLength));

    return addresses;
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

      int offset = 0xe2;

      text.append (String.format ("Disk number.... %04X%n", diskNumber));
      text.append (String.format ("Segments....... %04X%n%n", totDiskSegments));
      text.append (String.format (
          "Segment Logical   Physical   Length    -%04X     -%04X%n", offset, offset));

      int count = 1;
      for (DiskSegment segment : diskSegments)
        text.append (String.format ("  %02X  %s %n", count++, segment.toString (offset)));

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
      return String.format ("    %04X      %04X      %04X", logicalBlock, physicalBlock,
          segmentLength);
    }

    public String toString (int offset)
    {
      int logical = logicalBlock - offset;
      int physical = physicalBlock - offset;
      if (physical >= 0)
        return String.format ("    %04X      %04X      %04X      %04X      %04X",
            logicalBlock, physicalBlock, segmentLength, logical, physical);
      return String.format ("    %04X      %04X      %04X", logicalBlock, physicalBlock,
          segmentLength);
    }
  }

  class MultiDiskAddress
  {
    int diskNumber;
    int blockNumber;
    int totalBlocks;

    public MultiDiskAddress (int diskNumber, int blockNumber, int totalBlocks)
    {
      this.diskNumber = diskNumber;
      this.blockNumber = blockNumber;
      this.totalBlocks = totalBlocks;
    }

    @Override
    public String toString ()
    {
      return String.format ("%d:%03X", diskNumber, blockNumber);
    }
  }
}