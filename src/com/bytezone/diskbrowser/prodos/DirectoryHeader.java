package com.bytezone.diskbrowser.prodos;

import com.bytezone.diskbrowser.HexFormatter;

abstract class DirectoryHeader extends CatalogEntry
{
	int entryLength;
	int entriesPerBlock;
	int fileCount;

	public DirectoryHeader (ProdosDisk parentDisk, byte[] entryBuffer)
	{
		super (parentDisk, entryBuffer);

		entryLength = HexFormatter.intValue (entryBuffer[31]);
		entriesPerBlock = HexFormatter.intValue (entryBuffer[32]);
		fileCount = HexFormatter.intValue (entryBuffer[33], entryBuffer[34]);
	}
}