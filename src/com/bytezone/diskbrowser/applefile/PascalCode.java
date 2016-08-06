package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class PascalCode extends AbstractFile
    implements PascalConstants, Iterable<PascalSegment>
{
  private final List<PascalSegment> segments = new ArrayList<PascalSegment> (16);
  private final String comment;
  //  private final int blockOffset;
  //  private final Relocator relocator;

  public static void print ()
  {
    for (int i = 0; i < 216; i++)
      System.out.printf ("%3d  %d  %3s  %s%n", i + 128, PascalConstants.mnemonicSize[i],
          PascalConstants.mnemonics[i], PascalConstants.descriptions[i]);
  }

  public PascalCode (String name, byte[] buffer, int blockOffset, Relocator relocator)
  {
    super (name, buffer);

    SegmentDictionary segmentDictionary = new SegmentDictionary (name, buffer);
    if (!segmentDictionary.isValid ())
      throw new FileFormatException ("Error in PascalSegment");
    //    this.blockOffset = blockOffset;
    //    this.relocator = relocator;
    if (relocator != null)
      relocator.getMultiDiskAddress ("SEG-DIC", blockOffset, 1);

    int nonameCounter = 0;

    // Create segment list (up to 16 segments)
    for (int i = 0; i < 16; i++)
    {
      String codeName = HexFormatter.getString (buffer, 0x40 + i * 8, 8).trim ();
      int size = HexFormatter.intValue (buffer[i * 4 + 2], buffer[i * 4 + 3]);
      if (codeName.length () == 0 && size > 0)
        codeName = "<NULL" + ++nonameCounter + ">";
      if (size > 0)
      {
        // this could throw an exception
        PascalSegment pascalSegment =
            new PascalSegment (codeName, buffer, i, blockOffset, relocator);
        segments.add (pascalSegment);
      }
    }

    comment = HexFormatter.getPascalString (buffer, 0x1B0);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder (getHeader ());

    text.append ("Segment Dictionary\n==================\n\n");

    text.append ("Slot Addr Blks Byte   Name     Kind"
        + "            Txt Seg Mch Ver I/S I/S Disk:Block\n");
    text.append ("---- ---- ---- ----  --------  ---------------"
        + " --- --- --- --- --- --- ---------------------\n");

    for (PascalSegment segment : segments)
      text.append (segment.toText () + "\n");

    text.append ("\nComment : " + comment);

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