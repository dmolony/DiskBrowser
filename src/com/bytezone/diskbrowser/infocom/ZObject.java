package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.applefile.AbstractFile;

class ZObject extends AbstractFile
{
	static final int HEADER_SIZE = 9;

	int id;
	int startPtr;
	int propertyTablePtr;
	int propertyTableLength;
	int parent, sibling, child;
	List<Property> properties = new ArrayList<Property> ();
	Header header;
	BitSet attributes = new BitSet (32);

	public ZObject (String name, byte[] buffer, int offset, int seq, Header header)
	{
		super (name, buffer);
		this.header = header;

		startPtr = offset;
		id = seq;

		// attributes
		int bitIndex = 0;
		for (int i = 0; i < 4; i++)
		{
			byte b = buffer[offset + i];
			for (int j = 0; j < 8; j++)
			{
				if ((b & 0x80) == 0x80)
					attributes.set (bitIndex);
				b <<= 1;
				++bitIndex;
			}
		}

		// object's relatives
		parent = header.getByte (offset + 4);
		sibling = header.getByte (offset + 5);
		child = header.getByte (offset + 6);

		// the property header contains the object's short name
		propertyTablePtr = header.getWord (offset + 7);
		int ptr = propertyTablePtr;
		int nameLength = header.getByte (ptr) * 2;
		this.name = nameLength == 0 ? "<<none>>" : new ZString (buffer, ++ptr, header).value;
		ptr += nameLength;

		// read each property
		while (buffer[ptr] != 0)
		{
			Property p = new Property (buffer, ptr);
			properties.add (p);
			ptr += p.length + 1;
		}
		propertyTableLength = ptr - propertyTablePtr;
	}

	@Override
	public String getText ()
	{
		StringBuilder text = new StringBuilder ();

		text.append (String.format ("ID       : %3d   %s%n%nAttributes : ", id, name));
		text.append (HexFormatter.getHexString (buffer, startPtr, 4));
		text.append ("   " + attributes.toString ());

		String obj1 = parent == 0 ? "" : header.objectManager.list.get (parent - 1).name;
		String obj2 = sibling == 0 ? "" : header.objectManager.list.get (sibling - 1).name;
		String obj3 = child == 0 ? "" : header.objectManager.list.get (child - 1).name;

		text.append (String.format ("%n%nParent   : %02X  (%3d)  %s%n", parent, parent, obj1));
		text.append (String.format ("Sibling  : %02X  (%3d)  %s%n", sibling, sibling, obj2));
		text.append (String.format ("Child    : %02X  (%3d)  %s%n%n", child, child, obj3));

		for (Property prop : properties)
			text.append (prop + "\n");

		return text.toString ();
	}

	@Override
	public String getHexDump ()
	{
		StringBuilder text = new StringBuilder ("Header :\n\n");
		text.append (HexFormatter.formatNoHeader (buffer, startPtr, HEADER_SIZE));
		text.append ("\n\nProperty table:\n\n");
		text.append (HexFormatter.formatNoHeader (buffer, propertyTablePtr, propertyTableLength));
		return text.toString ();
	}

	Property getProperty (int id)
	{
		for (Property p : properties)
			if (p.propertyNumber == id)
				return p;
		return null;
	}

	@Override
	public String toString ()
	{
		return HexFormatter.getHexString (buffer, startPtr, HEADER_SIZE) + "   " + name;
	}

	class Property
	{
		int propertyNumber;
		int ptr;
		int length;

		int offset; //  only used if length == 2

		public Property (byte[] buffer, int ptr)
		{
			this.ptr = ptr;
			length = header.getByte (ptr) / 32 + 1;
			propertyNumber = header.getByte (ptr) % 32;

			if (length == 2)
				offset = header.getWord (ptr + 1) * 2;
		}

		private ZObject getObject ()
		{
			return header.objectManager.list.get ((buffer[ptr + 1] & 0xFF) - 1);
		}

		@Override
		public String toString ()
		{
			StringBuilder text =
						new StringBuilder (String.format ("%5s : ", header.propertyNames[propertyNumber]));

			String propertyType = header.propertyNames[propertyNumber];

			if (!propertyType.equals ("DICT") && !propertyType.contains ("STR"))
				text.append (String.format ("%-20s", HexFormatter.getHexString (buffer, ptr + 1, length)));

			if (propertyType.charAt (0) >= 'a') // directions are in lowercase
			{
				switch (length)
				{
					case 1:
						text.append (getObject ().name);
						break;
					case 2:
						text.append ("\"" + header.stringManager.stringAt (offset) + "\"");
						break;
					case 3:
						int address = header.getWord (ptr + 1) * 2;
						text.append (String.format ("R:%05X", address));
						break;
					case 4:
						address = header.getWord (ptr + 3) * 2;
						if (address > 0)
							text.append ("\"" + header.stringManager.stringAt (address) + "\"");
						break;
					default:
						break;
				}
			}
			else if (propertyType.equals ("DICT"))
			{
				for (int i = 1; i <= length; i += 2)
				{
					int address = header.getWord (ptr + i);
					text.append (header.wordAt (address) + ", ");
				}
				text.deleteCharAt (text.length () - 1);
				text.deleteCharAt (text.length () - 1);
			}
			else if (propertyType.startsWith ("CODE"))
			{
				if (offset > 0) // cretin contains 00 00
				{
					Routine r = header.codeManager.getRoutine (offset);
					if (r != null)
						text.append ("\n\n" + r.getText ());
					else
						// this can happen if the property is mislabelled as code
						text.append ("\n\n****** null routine\n");
				}
			}
			else if (propertyType.startsWith ("STR"))
			{
				text
							.append (String.format ("(%4X) \"%s\"", offset, header.stringManager
										.stringAt (offset)));
			}

			return text.toString ();
		}
	}
}