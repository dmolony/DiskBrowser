package com.bytezone.diskbrowser.prodos;

import java.util.List;

import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.Utility;;

// -----------------------------------------------------------------------------------//
public class SubDirectoryHeader extends DirectoryHeader
// -----------------------------------------------------------------------------------//
{
  private final int parentPointer;
  private final int parentSequence;
  private final int parentSize;

  private final int blockNo;

  // ---------------------------------------------------------------------------------//
  SubDirectoryHeader (ProdosDisk parentDisk, byte[] entryBuffer, FileEntry parent,
      int blockNo)
  // ---------------------------------------------------------------------------------//
  {
    super (parentDisk, entryBuffer, blockNo, 1);

    this.parentDirectory = parent.parentDirectory;
    this.blockNo = blockNo;

    parentPointer = Utility.unsignedShort (entryBuffer, 35);
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
  public int getBlockNo ()
  // ---------------------------------------------------------------------------------//
  {
    return blockNo;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%s  %04X:%02X", super.getText (), parentPointer,
        parentSequence);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    String locked = (access == 0x01) ? "*" : " ";
    return String.format ("   %s%-40s %15s", locked, "/" + name,
        created.format (ProdosDisk.df));
  }
}