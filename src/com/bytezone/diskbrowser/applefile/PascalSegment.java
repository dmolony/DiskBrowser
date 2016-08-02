package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class PascalSegment extends AbstractFile implements PascalConstants
{
  private final int segmentNoHeader;
  private int segmentNoBody;

  public final int blockNo;
  public final int size;

  private final int segKind;
  private final int textAddress;
  private final int machineType;
  private final int version;
  private final int intrinsSegs1;
  private final int intrinsSegs2;
  private final int slot;
  private int totalProcedures;
  private List<PascalProcedure> procedures;

  public PascalSegment (String name, byte[] fullBuffer, int seq)
  {
    super (name, fullBuffer);     // sets this.buffer to the full buffer temporarily

    this.slot = seq;
    this.blockNo = HexFormatter.intValue (fullBuffer[seq * 4], fullBuffer[seq * 4 + 1]);
    this.size = HexFormatter.intValue (fullBuffer[seq * 4 + 2], fullBuffer[seq * 4 + 3]);
    this.segmentNoHeader = fullBuffer[0x100 + seq * 2];

    segKind = HexFormatter.intValue (fullBuffer[0xC0 + seq * 2],
                                     fullBuffer[0xC0 + seq * 2 + 1]);
    textAddress = HexFormatter.intValue (fullBuffer[0xE0 + seq * 2],
                                         fullBuffer[0xE0 + seq * 2 + 1]);
    int flags = fullBuffer[0x101 + seq * 2] & 0xFF;
    machineType = flags & 0x0F;
    version = (flags & 0xD0) >> 5;
    intrinsSegs1 = HexFormatter.intValue (fullBuffer[0x120 + seq * 4],
                                          fullBuffer[0x120 + seq * 4 + 1]);
    intrinsSegs2 = HexFormatter.intValue (fullBuffer[0x120 + seq * 4 + 2],
                                          fullBuffer[0x120 + seq * 4 + 3]);

    int offset = blockNo * 512;
    //    System.out.printf ("Seq:%d, block:%d, size:%d, seg:%d, kind:%d, address:%d %n", seq,
    //                       blockNo, size, segmentNoHeader, segKind, textAddress);
    //    System.out.println (HexFormatter.format (fullBuffer));
    if (offset < fullBuffer.length)
    {
      buffer = new byte[size];      // replaces this.buffer with the segment buffer only
      System.arraycopy (fullBuffer, blockNo * 512, buffer, 0, size);
      totalProcedures = buffer[size - 1] & 0xFF;
      segmentNoBody = buffer[size - 2] & 0xFF;

      if (segmentNoHeader == 0)
        System.out.printf ("Zero segment header in %s seq %d%n", name, seq);
      else if (segmentNoBody != segmentNoHeader)
        System.out.println ("Segment number mismatch : " + segmentNoBody + " / "
            + segmentNoHeader);
    }
    else
    {
      System.out.printf ("Error in blocksize %,d > %,d for pascal disk%n", offset,
                         fullBuffer.length);
      throw new FileFormatException ("Error in PascalSegment");
    }
  }

  private void buildProcedureList ()
  {
    procedures = new ArrayList<PascalProcedure> (totalProcedures);

    for (int i = 1; i <= totalProcedures; i++)
      procedures.add (new PascalProcedure (buffer, i));
  }

  public String toText ()
  {
    return String
        .format (" %2d    %02X    %04X %,6d  %-8s  %-15s  %3d   %3d    %d     %d    %d    %d",
                 slot, blockNo, size, size, name, SegmentKind[segKind], textAddress,
                 segmentNoHeader, machineType, version, intrinsSegs1, intrinsSegs2);
  }

  @Override
  public String getText ()
  {
    if (procedures == null)
      buildProcedureList ();

    StringBuilder text = new StringBuilder ();
    String title = "Segment - " + name;
    text.append (title + "\n"
        + "===============================".substring (0, title.length ()) + "\n\n");
    String warning =
        segmentNoBody == segmentNoHeader ? "" : " (" + segmentNoHeader + " in header)";
    text.append (String.format ("Address........ %02X%n", blockNo));
    text.append (String.format ("Length......... %04X%n", buffer.length));
    text.append (String.format ("Machine type... %d%n", machineType));
    text.append (String.format ("Version........ %d%n", version));
    text.append (String.format ("Segment........ %d%s%n", segmentNoBody, warning));
    text.append (String.format ("Total procs.... %d%n", procedures.size ()));

    text.append ("\nProcedure Dictionary\n====================\n\n");

    int len = procedures.size () * 2 + 2;
    if (false)
      text.append (HexFormatter.format (buffer, buffer.length - len, len) + "\n\n");

    text.append ("Proc  Offset  Lvl  Entry   Exit   Parm   Data   Proc header\n");
    text.append ("----  ------  ---  -----   ----   ----   ----   --------------------\n");
    for (PascalProcedure procedure : procedures)
    {
      if (procedure.valid)
      {
        int address = size - procedure.slot * 2 - 2;
        text.append (String.format (
                                    " %2d    %04X  %3d    %04X   %04X   %04X   "
                                        + "%04X   (%04X - %04X = %04X)%n",
                                    procedure.procedureNo, procedure.offset,
                                    procedure.procLevel, procedure.codeStart,
                                    procedure.codeEnd, procedure.parmSize,
                                    procedure.dataSize, address, procedure.offset,
                                    procedure.procOffset));
      }
      else
        text.append (String.format (" %2d    %04X%n", procedure.slot, procedure.offset));
    }

    text.append ("\nStrings\n=======\n");
    for (PascalProcedure pp : procedures)
    {
      List<PascalCodeStatement> strings = pp.extractStrings ();
      for (PascalCodeStatement cs : strings)
        text.append (String.format (" %2d   %04X   %s%n", pp.procedureNo, cs.ptr,
                                    cs.text));
    }

    for (PascalProcedure procedure : procedures)
      if (procedure.valid)
        text.append (procedure);
    return text.toString ();
  }
}