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
    int diskSegments;
    List<Segment> segments = new ArrayList<Segment> ();

    public DiskRecord (byte[] buffer, int ptr)
    {
      diskNumber = HexFormatter.intValue (buffer[ptr], buffer[ptr + 1]);
      diskSegments = HexFormatter.intValue (buffer[ptr + 2], buffer[ptr + 4]);

      ptr += 4;
      for (int i = 0; i < diskSegments; i++)
      {
        segments.add (new Segment (buffer, ptr));
        ptr += 6;
      }
    }

    int size ()
    {
      return 4 + segments.size () * 6;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Disk number.... %04X%n", diskNumber));
      text.append (String.format ("Segments....... %04X%n%n", diskSegments));
      text.append ("Segment Logical   Physical   Length\n");

      int count = 1;
      for (Segment segment : segments)
        text.append (String.format ("  %02X  %s %n", count++, segment));

      return text.toString ();
    }
  }

  private class Segment
  {
    int logicalBlock;
    int physicalBlock;
    int segmentLength;

    public Segment (byte[] buffer, int ptr)
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
  }
}