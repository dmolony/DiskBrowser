package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.applefile.Relocator.MultiDiskAddress;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

public class PascalCode extends AbstractFile
    implements PascalConstants, Iterable<PascalSegment>
{
  private final List<PascalSegment> segments = new ArrayList<PascalSegment> (16);
  private final String comment;
  private final int blockOffset;
  private final Relocator relocator;

  public static void print ()
  {
    for (int i = 0; i < 216; i++)
      System.out.printf ("%3d  %d  %3s  %s%n", i + 128, PascalConstants.mnemonicSize[i],
          PascalConstants.mnemonics[i], PascalConstants.descriptions[i]);
  }

  public PascalCode (String name, byte[] buffer, int blockOffset, Relocator relocator)
  {
    super (name, buffer);

    this.blockOffset = blockOffset;
    this.relocator = relocator;

    int nonameCounter = 0;
    if (false)
    {
      //      System.out.println (name);
      //      byte[] key = new byte[] { 0x38, 0x00, 0x0C, 0x1C };
      byte[] key = new byte[] { 0x0F };
      Utility.find (buffer, key);
    }

    // Create segment list (up to 16 segments)
    for (int i = 0; i < 16; i++)
    {
      String codeName = HexFormatter.getString (buffer, 0x40 + i * 8, 8).trim ();
      int size = HexFormatter.intValue (buffer[i * 4 + 2], buffer[i * 4 + 3]);
      if (codeName.length () == 0 && size > 0)
        codeName = "<NULL" + ++nonameCounter + ">";
      //   System.out.printf ("%s %s %d %n", HexFormatter.getHexString (buffer, i * 4, 4),
      //                         codeName, size);
      if (size > 0)
        segments.add (new PascalSegment (codeName, buffer, i));
    }
    comment = HexFormatter.getPascalString (buffer, 0x1B0);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder (getHeader ());

    text.append ("Segment Dictionary\n==================\n\n");

    text.append ("Slot Addr Addr Blks  Len   D:Blk    Name     Kind"
        + "            Txt  Seg  Mch Ver  I/S  I/S\n");
    text.append ("---- ---- ---- ----  ----  -----  --------  ---------------"
        + " ---  ---  --- ---  ---  ---\n");

    MultiDiskAddress lastMultiDiskAddress = null;
    int minBlocks = 0;
    int maxBlocks = 0;

    for (PascalSegment segment : segments)
    {
      int sizeInBlocks = (segment.size - 1) / 512 + 1;
      String multiDiskAddressText = "";
      if (segment.segmentNoHeader == 1)           // main segment
      {
        multiDiskAddressText = String.format ("1:%03X", (segment.blockNo + blockOffset));
      }
      else if (relocator != null)
      {
        if (segment.blockNo >= minBlocks && segment.blockNo < maxBlocks)
        {
          int offset = segment.blockNo - minBlocks;
          multiDiskAddressText = String.format ("%d:%03X",
              lastMultiDiskAddress.diskNumber, lastMultiDiskAddress.blockNumber + offset);
        }
        else
        {
          int targetBlock = segment.blockNo + blockOffset;
          List<MultiDiskAddress> addresses = relocator.getMultiDiskAddress (targetBlock);
          if (addresses.isEmpty ())
            multiDiskAddressText = ".";
          else
          {
            lastMultiDiskAddress = addresses.get (0);
            multiDiskAddressText = addresses.get (0).toString ();
            if (lastMultiDiskAddress.totalBlocks > sizeInBlocks)
            {
              minBlocks = segment.blockNo;
              maxBlocks = minBlocks + lastMultiDiskAddress.totalBlocks;
            }
          }
        }
      }
      text.append (segment.toText (blockOffset, multiDiskAddressText) + "\n");
    }
    text.append ("\nComment : " + comment + "\n\n");

    return text.toString ();
  }

  private String getHeader ()
  {
    return "Name : " + name + "\n\n";
  }

  @Override
  public Iterator<PascalSegment> iterator ()
  {
    return segments.iterator ();
  }
}