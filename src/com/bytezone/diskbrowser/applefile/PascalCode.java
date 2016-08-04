package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

public class PascalCode extends AbstractFile
    implements PascalConstants, Iterable<PascalSegment>
{
  List<PascalSegment> segments = new ArrayList<PascalSegment> (16);
  String codeName;
  String comment;

  public static void print ()
  {
    for (int i = 0; i < 216; i++)
      System.out.printf ("%3d  %d  %3s  %s%n", i + 128, PascalConstants.mnemonicSize[i],
                         PascalConstants.mnemonics[i], PascalConstants.descriptions[i]);
  }

  public PascalCode (String name, byte[] buffer)
  {
    super (name, buffer);
    int nonameCounter = 0;
    if (false)
    {
      System.out.println (name);
      //      byte[] key = new byte[] { 0x38, 0x00, 0x0C, 0x1C };
      byte[] key = new byte[] { 0x0F };
      Utility.find (buffer, key);
    }

    // Create segment list (up to 16 segments)
    for (int i = 0; i < 16; i++)
    {
      codeName = HexFormatter.getString (buffer, 0x40 + i * 8, 8).trim ();
      if (codeName.length () == 0)
        codeName = "<NULL" + nonameCounter++ + ">";
      int size = HexFormatter.intValue (buffer[i * 4 + 2], buffer[i * 4 + 3]);
      //      System.out.printf ("%s %s %d %n", HexFormatter.getHexString (buffer, i * 4, 4),
      //                         codeName, size);
      if (size > 0)
      {
        try
        {
          segments.add (new PascalSegment (codeName, buffer, i));
        }
        catch (FileFormatException e)
        {
          System.out.printf ("Bad segment: %d%n", i);
        }
      }
    }
    comment = HexFormatter.getPascalString (buffer, 0x1B0);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder (getHeader ());

    text.append ("Segment Dictionary\n==================\n\n");

    text.append ("Slot Addr Blks  Len    Len    Name     Kind"
        + "            Txt  Seg  Mch Ver  I/S  I/S\n");
    text.append ("---- ---- ----  ----  -----  --------  ---------------"
        + " ---  ---  --- ---  ---  ---\n");

    for (PascalSegment segment : segments)
      text.append (segment.toText () + "\n");
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