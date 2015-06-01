package com.bytezone.diskbrowser.prodos;

import java.util.List;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;

class SubDirectoryHeader extends DirectoryHeader
{
	int parentPointer;
	int parentSequence;
	int parentSize;

	public SubDirectoryHeader (ProdosDisk parentDisk, byte[] entryBuffer, FileEntry parent)
	{
		super (parentDisk, entryBuffer);
		this.parentDirectory = parent.parentDirectory;

		parentPointer = HexFormatter.intValue (entryBuffer[35], entryBuffer[36]);
		parentSequence = HexFormatter.intValue (entryBuffer[37]);
		parentSize = HexFormatter.intValue (entryBuffer[38]);
	}

	@Override
	public String toString ()
	{
		String locked = (access == 0x01) ? "*" : " ";
		return String.format ("   %s%-40s %15s", locked, "/" + name, ProdosDisk.df.format (created
					.getTime ()));
	}

	public DataSource getDataSource ()
	{
		// should this return a directory listing?
		return null;
	}

	public List<DiskAddress> getSectors ()
	{
		return null;
	}
}