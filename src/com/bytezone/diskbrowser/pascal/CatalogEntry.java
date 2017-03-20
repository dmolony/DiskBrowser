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

abstract class CatalogEntry implements AppleFileSource
{
  protected AbstractFile file;
  protected final PascalDisk parent;
  protected String name;
  protected int firstBlock;
  protected int lastBlock;                      // block AFTER last used block
  protected int fileType;
  protected GregorianCalendar date;
  protected int bytesUsedInLastBlock;
  protected final List<DiskAddress> blocks = new ArrayList<DiskAddress> ();

  public CatalogEntry (PascalDisk parent, byte[] buffer)
  {
    this.parent = parent;

    firstBlock = HexFormatter.intValue (buffer[0], buffer[1]);
    lastBlock = HexFormatter.intValue (buffer[2], buffer[3]);
    //    fileType = HexFormatter.intValue (buffer[4], buffer[5]);
    fileType = buffer[4] & 0xFF;
    name = HexFormatter.getPascalString (buffer, 6);
    bytesUsedInLastBlock = HexFormatter.intValue (buffer[16], buffer[17]);

    Disk disk = parent.getDisk ();
    int max = Math.min (lastBlock, disk.getTotalBlocks ());
    for (int i = firstBlock; i < max; i++)
      blocks.add (disk.getDiskAddress (i));
  }

  @Override
  public boolean contains (DiskAddress da)
  {
    for (DiskAddress sector : blocks)
      if (sector.matches (da))
        return true;
    return false;
  }

  @Override
  public List<DiskAddress> getSectors ()
  {
    List<DiskAddress> sectors = new ArrayList<DiskAddress> (blocks);
    return sectors;
  }

  @Override
  public FormattedDisk getFormattedDisk ()
  {
    return parent;
  }

  @Override
  public String getUniqueName ()
  {
    return name;
  }

  @Override
  public String toString ()
  {
    int size = lastBlock - firstBlock;
    String fileTypeText = fileType < 0 || fileType >= parent.fileTypes.length ? "????"
        : parent.fileTypes[fileType];
    return String.format ("%03d  %s  %-15s", size, fileTypeText, name);
  }
}