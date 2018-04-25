package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class PascalSegment extends AbstractFile implements PascalConstants
{
  private final static int BLOCK_SIZE = 512;
  final int segmentNoHeader;
  private int segmentNoBody;
  //  private final int blockOffset;
  //  private final Relocator relocator;
  boolean debug = false;

  public int blockNo;
  //  public int newBlockNo;
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
  //  private List<MultiDiskAddress> addresses;

  public PascalSegment (String name, byte[] fullBuffer, int seq, int blockOffset)
  {
    super (name, fullBuffer);     // sets this.buffer to the full buffer temporarily

    this.slot = seq;
    //    this.blockOffset = blockOffset;
    //    this.relocator = relocator;

    this.blockNo = HexFormatter.intValue (fullBuffer[seq * 4], fullBuffer[seq * 4 + 1]);
    this.size = HexFormatter.intValue (fullBuffer[seq * 4 + 2], fullBuffer[seq * 4 + 3]);

    segKind = HexFormatter.intValue (fullBuffer[0xC0 + seq * 2],
        fullBuffer[0xC0 + seq * 2 + 1]);

    textAddress = HexFormatter.intValue (fullBuffer[0xE0 + seq * 2],
        fullBuffer[0xE0 + seq * 2 + 1]);

    // segment 1 is the main segment, 2-6 are used by the system, and 7
    // onwards is for the program
    this.segmentNoHeader = fullBuffer[0x100 + seq * 2] & 0xFF;
    int flags = fullBuffer[0x101 + seq * 2] & 0xFF;

    // 0 unknown,
    // 1 positive byte sex p-code
    // 2 negative byte sex p-code (apple pascal)
    // 3-9 6502 code (7 = apple 6502)
    machineType = flags & 0x0F;

    version = (flags & 0xD0) >> 5;

    intrinsSegs1 = HexFormatter.intValue (fullBuffer[0x120 + seq * 4],
        fullBuffer[0x120 + seq * 4 + 1]);
    intrinsSegs2 = HexFormatter.intValue (fullBuffer[0x120 + seq * 4 + 2],
        fullBuffer[0x120 + seq * 4 + 3]);

    int offset = blockNo * 512;

    //    if (relocator != null)
    //    {
    //      //      if (segmentNoHeader > 1)
    //      //      {
    //      int sizeInBlocks = (size - 1) / BLOCK_SIZE + 1;
    //      int targetBlock = blockNo + blockOffset;
    //      addresses = relocator.getMultiDiskAddress (name, targetBlock, sizeInBlocks);
    //      if (addresses.size () > 0)
    //      {
    //        MultiDiskAddress multiDiskAddress = addresses.get (0);
    //        if (multiDiskAddress.diskNumber == 1)
    //          offset = (multiDiskAddress.physicalBlockNumber - blockOffset) * BLOCK_SIZE;
    //        else
    //          offset = -1;
    //      }
    //      //      }
    //    }

    if (offset < 0)
    {
      buffer = new byte[0];
    }
    else if ((offset + size) < fullBuffer.length)
    {
      buffer = new byte[size];      // replaces this.buffer with the segment buffer only
      System.arraycopy (fullBuffer, offset, buffer, 0, size);
      totalProcedures = buffer[size - 1] & 0xFF;
      segmentNoBody = buffer[size - 2] & 0xFF;

      if (debug)
        if (segmentNoHeader == 0)
          System.out.printf ("Zero segment header in %s seq %d%n", name, seq);
        else if (segmentNoBody != segmentNoHeader)
          System.out.println (
              "Segment number mismatch : " + segmentNoBody + " / " + segmentNoHeader);
    }
    else
    {
      throw new FileFormatException ("Error in PascalSegment");
    }
  }

  //  void setMultiDiskAddresses (List<MultiDiskAddress> addresses)
  //  {
  //    this.addresses = addresses;
  //  }

  private void buildProcedureList ()
  {
    procedures = new ArrayList<PascalProcedure> (totalProcedures);

    for (int i = 1; i <= totalProcedures; i++)
      procedures.add (new PascalProcedure (buffer, i));
  }

  public String toText ()
  {
    int sizeInBlocks = (size - 1) / BLOCK_SIZE + 1;

    return String.format (
        " %2d   %02X   %02X  %04X  %-8s  %-15s%3d   " + "%02X  %d   %d   %d   %d  %s",
        slot, blockNo, sizeInBlocks, size, name, SegmentKind[segKind], textAddress,
        segmentNoHeader, machineType, version, intrinsSegs1, intrinsSegs2,
        getMultiDiskAddresses ());
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
        : String.format (" (%02X in routine)", segmentNoBody);
    text.append (String.format ("Address........ %02X%n", blockNo));
    //    if (addresses != null)
    text.append (String.format ("Multi disk .... %s%n", getMultiDiskAddresses ()));
    text.append (String.format ("Length......... %04X%n", buffer.length));
    text.append (String.format ("Machine type... %d%n", machineType));
    text.append (String.format ("Version........ %d%n", version));
    text.append (String.format ("Segment........ %02X%s%n", segmentNoHeader, warning));
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

  private String getMultiDiskAddresses ()
  {
    String multiDiskAddressText = "";
    //    int sizeInBlocks = (size - 1) / BLOCK_SIZE + 1;

    //    if (segmentNoHeader == 1)           // main segment
    //    {
    //      multiDiskAddressText = String.format ("1:%03X", (blockNo + blockOffset));
    //    }
    //    else
    //    if (relocator != null)
    //    {
    //      int targetBlock = blockNo + blockOffset;
    //      List<MultiDiskAddress> addresses =
    //          relocator.getMultiDiskAddress (name, targetBlock, sizeInBlocks);
    //      if (addresses.isEmpty ())
    //        multiDiskAddressText = ".";
    //      else
    //      {
    //        StringBuilder locations = new StringBuilder ();
    //        for (MultiDiskAddress multiDiskAddress : addresses)
    //          locations.append (multiDiskAddress.toString () + ", ");
    //        if (locations.length () > 2)
    //        {
    //          locations.deleteCharAt (locations.length () - 1);
    //          locations.deleteCharAt (locations.length () - 1);
    //        }
    //        multiDiskAddressText = locations.toString ();
    //      }
    //    }
    return multiDiskAddressText;
  }
}