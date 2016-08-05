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

    text.append ("Slot Addr Blks Len    Name     Kind"
        + "            Txt Seg Mch Ver I/S I/S D:Blk\n");
    text.append ("---- ---- ---- ----  --------  ---------------"
        + " --- --- --- --- --- --- ---------------------\n");

    for (PascalSegment segment : segments)
    {
      int sizeInBlocks = (segment.size - 1) / 512 + 1;
      System.out.printf ("Seg: %-8s add: %03X%n", segment.name, segment.blockNo);
      String multiDiskAddressText = "";
      if (segment.segmentNoHeader == 1)           // main segment
      {
        multiDiskAddressText = String.format ("1:%03X", (segment.blockNo + blockOffset));
      }
      else if (relocator != null)
      {
        int targetBlock = segment.blockNo + blockOffset;
        List<MultiDiskAddress> addresses =
            relocator.getMultiDiskAddress (segment.name, targetBlock, sizeInBlocks);
        if (addresses.isEmpty ())
          multiDiskAddressText = ".";
        else
        {
          StringBuilder locations = new StringBuilder ();
          for (MultiDiskAddress multiDiskAddress : addresses)
            locations.append (multiDiskAddress.toString () + ", ");
          if (locations.length () > 2)
          {
            locations.deleteCharAt (locations.length () - 1);
            locations.deleteCharAt (locations.length () - 1);
          }
          multiDiskAddressText = locations.toString ();
        }
      }
      text.append (segment.toText (blockOffset, multiDiskAddressText) + "\n");
    }
    text.append ("\nComment : " + comment + "\n\n");
    relocator.list ();

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