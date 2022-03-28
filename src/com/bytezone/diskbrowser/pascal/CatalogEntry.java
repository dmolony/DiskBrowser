package com.bytezone.diskbrowser.pascal;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
abstract class CatalogEntry implements AppleFileSource
// -----------------------------------------------------------------------------------//
{
  protected AbstractFile file;
  protected final PascalDisk parent;
  protected String name;
  protected int firstBlock;
  protected int lastBlock;                      // block AFTER last used block
  protected int fileType;
  protected GregorianCalendar date;
  protected int bytesUsedInLastBlock;
  protected final List<DiskAddress> blocks = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  CatalogEntry (PascalDisk parent, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;

    firstBlock = Utility.getShort (buffer, 0);
    lastBlock = Utility.getShort (buffer, 2);
    fileType = buffer[4] & 0xFF;
    name = HexFormatter.getPascalString (buffer, 6);
    bytesUsedInLastBlock = Utility.getShort (buffer, 16);

    Disk disk = parent.getDisk ();
    int max = Math.min (lastBlock, disk.getTotalBlocks ());
    for (int i = firstBlock; i < max; i++)
      blocks.add (disk.getDiskAddress (i));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean contains (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    for (DiskAddress sector : blocks)
      if (sector.matches (da))
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getSectors ()
  // ---------------------------------------------------------------------------------//
  {
    return new ArrayList<> (blocks);       // make a copy
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public FormattedDisk getFormattedDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return parent;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getUniqueName ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    int size = lastBlock - firstBlock;
    String fileTypeText =
        fileType < 0 || fileType >= parent.fileTypes.length ? "????" : parent.fileTypes[fileType];
    return String.format ("%03d  %s  %-15s", size, fileTypeText, name);
  }
}