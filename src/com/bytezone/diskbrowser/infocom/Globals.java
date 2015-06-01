package com.bytezone.diskbrowser.infocom;

import com.bytezone.diskbrowser.applefile.AbstractFile;

class Globals extends AbstractFile
{
	static final int TOTAL_GLOBALS = 240;
	Header header;
	int globalsPtr, globalsSize;
	int arrayPtr, arraySize;

	public Globals (Header header)
	{
		super ("Globals", header.buffer);
		this.header = header;

		globalsPtr = header.globalsOffset;
		globalsSize = TOTAL_GLOBALS * 2;
		arrayPtr = globalsPtr + globalsSize;
		arraySize = header.staticMemory - arrayPtr;

		// add entries for AbstractFile.getHexDump ()
		hexBlocks.add (new HexBlock (globalsPtr, globalsSize, "Globals:"));
		hexBlocks.add (new HexBlock (arrayPtr, arraySize, "Arrays:"));
	}

	@Override
	public String getText ()
	{
		StringBuilder text = new StringBuilder ();
		for (int i = 1; i <= TOTAL_GLOBALS; i++)
		{
			int value = header.getWord (globalsPtr + i * 2);
			text.append (String.format ("G%03d    %04X    ", i, value));
			int address = value * 2;
			if (address >= header.stringPointer && address < header.fileLength)
				text.append (header.stringManager.stringAt (address) + "\n");
			else
				text.append (String.format ("%,6d%n", value));
		}
		text.deleteCharAt (text.length () - 1);
		return text.toString ();
	}
}