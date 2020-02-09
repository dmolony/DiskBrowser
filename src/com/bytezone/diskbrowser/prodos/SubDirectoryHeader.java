package com.bytezone.diskbrowser.prodos;

import java.util.List;

import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class SubDirectoryHeader extends DirectoryHeader
// -----------------------------------------------------------------------------------//
{
  private final int parentPointer;
  private final int parentSequence;
  private final int parentSize;

  // ---------------------------------------------------------------------------------//
  SubDirectoryHeader (ProdosDisk parentDisk, byte[] entryBuffer, FileEntry parent)
  // ---------------------------------------------------------------------------------//
  {
    super (parentDisk, entryBuffer);
    this.parentDirectory = parent.parentDirectory;

    parentPointer = HexFormatter.intValue (entryBuffer[35], entryBuffer[36]);
    parentSequence = entryBuffer[37] & 0xFF;
    parentSize = entryBuffer[38] & 0xFF;

    if (false)
      System.out.printf ("", parentPointer, parentSequence, parentSize);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getDataSource ()
  // ---------------------------------------------------------------------------------//
  {
    // should this return a directory listing?
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getSectors ()
  // ---------------------------------------------------------------------------------//
  {
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    String locked = (access == 0x01) ? "*" : " ";
    return String.format ("   %s%-40s %15s", locked, "/" + name,
        parentDisk.df.format (created.getTime ()));
  }
}