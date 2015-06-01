package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.HexFormatter;

public class PascalCode extends AbstractFile implements PascalConstants, Iterable<PascalSegment>
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

		// Build segment list (up to 16 segments)
		for (int i = 0; i < 16; i++)
		{
			codeName = HexFormatter.getString (buffer, 0x40 + i * 8, 8).trim ();
			int size = HexFormatter.intValue (buffer[i * 4 + 2], buffer[i * 4 + 3]);
			if (size > 0)
			{
				if (codeName.length () == 0)
					codeName = "<NULL" + nonameCounter++ + ">";
				segments.add (new PascalSegment (codeName, buffer, i));
			}
		}
		comment = HexFormatter.getPascalString (buffer, 0x1B0);
	}

	public String getText ()
	{
		StringBuilder text = new StringBuilder (getHeader ());

		text.append ("Segment Dictionary\n==================\n\n");

		text.append ("Slot  Addr    Len    Len    Name    Kind"
								+ "            Text  Seg#  Mtyp  Vers  I/S\n");
		text.append ("----  ----  -----  -----  --------  ---------------"
								+ " ----  ----  ----  ----  ---\n");

		for (PascalSegment segment : segments)
			text.append (segment.toText () + "\n");
		text.append ("\nComment : " + comment + "\n\n");

		return text.toString ();
	}

	private String getHeader ()
	{
		return "Name : " + name + "\n\n";
	}

	public Iterator<PascalSegment> iterator ()
	{
		return segments.iterator ();
	}
}