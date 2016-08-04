package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class PascalSegment extends AbstractFile implements PascalConstants
{
  private final int segmentNoHeader;
  private int segmentNoBody;

  public int blockNo;
  public int newBlockNo;
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
  private static final List<Redirection> redirections = new ArrayList<Redirection> ();

  static
  {
    // somehow this should match the data in SYSTEM.RELOC
    redirections.add (new Redirection ("WIZARDRY", 0x01, 0x1C66, 0x01));
    redirections.add (new Redirection ("KANJIREA", 0x013F, 0x104E, 0x10));
    redirections.add (new Redirection ("UTILITIE", 0x48, 0x1598, 0x19));
    redirections.add (new Redirection ("SHOPS", 0x53, 0x0BE2, 0x24));
    redirections.add (new Redirection ("CAMP", 0x70, 0x24CA, 0x2A));
    redirections.add (new Redirection ("DOCOPY", 0x83, 0x07A0, 0x3D));
    redirections.add (new Redirection ("DOCACHE", 0x87, 0x072E, 0x41));
  }

  public PascalSegment (String name, byte[] fullBuffer, int seq)
  {
    super (name, fullBuffer);     // sets this.buffer to the full buffer temporarily

    this.slot = seq;
    this.blockNo = HexFormatter.intValue (fullBuffer[seq * 4], fullBuffer[seq * 4 + 1]);
    this.size = HexFormatter.intValue (fullBuffer[seq * 4 + 2], fullBuffer[seq * 4 + 3]);

    for (Redirection redirection : redirections)
      if (redirection.matches (name, blockNo, size))
      {
        newBlockNo = redirection.newOffset;
        break;
      }

    segKind = HexFormatter.intValue (fullBuffer[0xC0 + seq * 2],
        fullBuffer[0xC0 + seq * 2 + 1]);

    textAddress = HexFormatter.intValue (fullBuffer[0xE0 + seq * 2],
        fullBuffer[0xE0 + seq * 2 + 1]);

    // segment 1 is the main segment, 2-6 are used by the system, and 7
    // onwards is for our program
    this.segmentNoHeader = fullBuffer[0x100 + seq * 2] & 0xFF;
    int flags = fullBuffer[0x101 + seq * 2] & 0xFF;
    machineType = flags & 0x0F;
    version = (flags & 0xD0) >> 5;
    intrinsSegs1 = HexFormatter.intValue (fullBuffer[0x120 + seq * 4],
        fullBuffer[0x120 + seq * 4 + 1]);
    intrinsSegs2 = HexFormatter.intValue (fullBuffer[0x120 + seq * 4 + 2],
        fullBuffer[0x120 + seq * 4 + 3]);

    int offset = blockNo * 512;
    if (newBlockNo > 0)
      offset = newBlockNo * 512;
    //    System.out.printf ("Seq:%d, block:%d, size:%d, seg:%d, kind:%d, address:%d %n", seq,
    //                       blockNo, size, segmentNoHeader, segKind, textAddress);
    //    System.out.println (HexFormatter.format (fullBuffer));
    if (offset < fullBuffer.length)
    {
      buffer = new byte[size];      // replaces this.buffer with the segment buffer only
      System.arraycopy (fullBuffer, offset, buffer, 0, size);
      totalProcedures = buffer[size - 1] & 0xFF;
      segmentNoBody = buffer[size - 2] & 0xFF;

      if (segmentNoHeader == 0)
        System.out.printf ("Zero segment header in %s seq %d%n", name, seq);
      else if (segmentNoBody != segmentNoHeader)
        System.out.println (
            "Segment number mismatch : " + segmentNoBody + " / " + segmentNoHeader);
    }
    else
    {
      //      System.out.printf ("Error in blocksize %,d > %,d for pascal disk%n", offset,
      //                         fullBuffer.length);
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
    int sizeInBlocks = (size - 1) / 512 + 1;
    String newBlock = newBlockNo > 0 ? String.format ("%02X + %02X = %02X", newBlockNo,
        sizeInBlocks, (newBlockNo + sizeInBlocks)) : "";
    return String.format (
        " %2d   %02X   %02X   %04X %,6d  %-8s  %-15s%3d    "
            + "%02X   %d   %d    %d    %d    %s",
        slot, blockNo, sizeInBlocks, size, size, name, SegmentKind[segKind], textAddress,
        segmentNoHeader, machineType, version, intrinsSegs1, intrinsSegs2, newBlock);
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
    String warning = segmentNoBody == segmentNoHeader ? ""
        : String.format (" (%02X in header)", segmentNoHeader);
    text.append (String.format ("Address........ %02X%n", blockNo));
    text.append (String.format ("Length......... %04X%n", buffer.length));
    text.append (String.format ("Machine type... %d%n", machineType));
    text.append (String.format ("Version........ %d%n", version));
    text.append (String.format ("Segment........ %02X%s%n", segmentNoBody, warning));
    text.append (String.format ("Total procs.... %d%n", procedures.size ()));

    text.append ("\nProcedure Dictionary\n====================\n\n");

    int len = procedures.size () * 2 + 2;
    if (false)
      text.append (HexFormatter.format (buffer, buffer.length - len, len) + "\n\n");

    text.append ("Proc  Offset  Lvl  Entry   Exit   Parm   Data   Proc header\n");
    text.append (
        "----  ------  ---  -----   ----   ----   ----   --------------------\n");
    for (PascalProcedure procedure : procedures)
    {
      if (procedure.valid)
      {
        int address = size - procedure.slot * 2 - 2;
        text.append (String.format (
            " %3d   %04X   %3d   %04X   %04X   %04X   %04X   (%04X - %04X = %04X)%n",
            procedure.procedureNo, procedure.offset, procedure.procLevel,
            procedure.codeStart, procedure.codeEnd, procedure.parmSize,
            procedure.dataSize, address, procedure.offset, procedure.procOffset));
      }
      else
        text.append (String.format (" %3d   %04X%n", procedure.slot, procedure.offset));
    }

    text.append ("\nStrings\n=======\n");
    for (PascalProcedure pp : procedures)
    {
      List<PascalCodeStatement> strings = pp.extractStrings ();
      for (PascalCodeStatement cs : strings)
        text.append (
            String.format (" %2d   %04X   %s%n", pp.procedureNo, cs.ptr, cs.text));
    }

    for (PascalProcedure procedure : procedures)
      if (procedure.valid)
        text.append (procedure);
    return text.toString ();
  }
}

class Redirection
{
  int oldOffset;
  int newOffset;
  int length;
  String name;

  public Redirection (String name, int oldOffset, int length, int newOffset)
  {
    this.name = name;
    this.oldOffset = oldOffset;
    this.newOffset = newOffset;
    this.length = length;
  }

  public boolean matches (String name, int offset, int length)
  {
    return this.name.equals (name) && this.oldOffset == offset && this.length == length;
  }
}