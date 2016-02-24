package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class SimpleText2 extends AbstractFile
{
	List<Integer> lineStarts = new ArrayList<Integer> ();
	int loadAddress;
	boolean showByte = false;

	public SimpleText2 (String name, byte[] buffer, int loadAddress)
	{
		super (name, buffer);
		this.loadAddress = loadAddress;

		// store a pointer to each new line
		int ptr = 0;
		while (buffer[ptr] != -1)
		{
			int length = buffer[ptr] & 0xFF;
			lineStarts.add (ptr);
			ptr += length + 1;
		}
	}

	public String getText ()
	{
		StringBuilder text = new StringBuilder ();

		text.append ("Name    : " + name + "\n");
		text.append (String.format ("Length  : $%04X (%d)%n", buffer.length, buffer.length));
		text.append (String.format ("Load at : $%04X%n%n", loadAddress));

		for (Integer i : lineStarts)
			text.append (String.format ("%05X  %s%n", i, getLine (i)));
		return text.toString ();
	}

	public String getHexDump ()
	{
		StringBuilder text = new StringBuilder ();

		for (Integer i : lineStarts)
			text.append (HexFormatter.formatNoHeader (buffer, i, (buffer[i] & 0xFF) + 1) + "\n");
		text.append (HexFormatter.formatNoHeader (buffer, buffer.length - 2, 2) + "\n");

		return text.toString ();
	}

	// convert buffer to text, ignore line-break at the end
	private String getLine (int ptr)
	{
		StringBuilder line = new StringBuilder ();
		int length = buffer[ptr] & 0xFF;
		while (--length > 0)
		{
			int val = buffer[++ptr] & 0xFF;
			if (val == 0xBB)
			{
				while (line.length () < 35)
					line.append (' ');
				line.append (';');
			}
			else if (val >= 0x80)
			{
				while (line.length () < 10)
					line.append (' ');
				if (val == 0xDC)
					line.append (String.format ("EQU", val));
				else if (val == 0xD0)
					line.append (String.format ("STA", val));
				else if (val == 0xD2)
					line.append (String.format ("STY", val));
				else if (val == 0xD4)
					line.append (String.format ("LSR", val));
				else if (val == 0xD5)
					line.append (String.format ("ROR", val));
				else if (val == 0xD7)
					line.append (String.format ("ASL", val));
				else if (val == 0xD9)
					line.append (String.format ("EQ ", val));
				else if (val == 0xDB)
					line.append (String.format ("TGT", val));
				else if (val == 0xDA)
					line.append (String.format ("ORG", val));
				else if (val == 0xB1)
					line.append (String.format ("TYA", val));
				else if (val == 0xC1)
					line.append (String.format ("AND", val));
				else if (val == 0xC4)
					line.append (String.format ("CMP", val));
				else if (val == 0xC8)
					line.append (String.format ("EOR", val));
				else if (val == 0xCA)
					line.append (String.format ("JMP", val));
				else if (val == 0xCB)
					line.append (String.format ("JSR", val));
				else if (val == 0xCD)
					line.append (String.format ("LDA", val));
				else if (val == 0xCE)
					line.append (String.format ("LDX", val));
				else if (val == 0xCF)
					line.append (String.format ("LDY", val));
				else if (val == 0xA1)
					line.append (String.format ("PHA", val));
				else if (val == 0xA2)
					line.append (String.format ("PLA", val));
				else if (val == 0xA5)
					line.append (String.format ("RTS", val));
				else if (val == 0xA9)
					line.append (String.format ("SEC", val));
				else if (val == 0xAD)
					line.append (String.format ("TAY", val));
				else if (val == 0x82)
					line.append (String.format ("BMI", val));
				else if (val == 0x84)
					line.append (String.format ("BCS", val));
				else if (val == 0x85)
					line.append (String.format ("BPL", val));
				else if (val == 0x86)
					line.append (String.format ("BNE", val));
				else if (val == 0x87)
					line.append (String.format ("BEQ", val));
				else if (val == 0x99)
					line.append (String.format ("CLC", val));
				else if (val == 0x9C)
					line.append (String.format ("DEX", val));
				else if (val == 0x9F)
					line.append (String.format ("INY", val));
				else
					line.append (String.format (".%02X.", val));

				line.append (' ');
				++ptr;
				if (buffer[ptr] < 0x20 && showByte)
				{
					val = buffer[ptr] & 0xFF;
					line.append (String.format (".%02X. ", val));
				}
			}
			else
				line.append ((char) val);
		}
		return line.toString ();
	}
}