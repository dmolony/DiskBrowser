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
  protected final PascalDisk parent;
  String name;
  int firstBlock;
  int lastBlock;                      // block AFTER last used block
  int fileType;
  GregorianCalendar date;
  List<DiskAddress> blocks = new ArrayList<DiskAddress> ();
  AbstractFile file;

  public CatalogEntry (PascalDisk parent, byte[] buffer)
  {
    this.parent = parent;

    firstBlock = HexFormatter.intValue (buffer[0], buffer[1]);
    lastBlock = HexFormatter.intValue (buffer[2], buffer[3]);
    //      fileType = HexFormatter.intValue (buffer[4], buffer[5]);
    fileType = buffer[4] & 0x0F;
    name = HexFormatter.getPascalString (buffer, 6);

    Disk disk = parent.getDisk ();
    for (int i = firstBlock; i < lastBlock; i++)
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
  public String toString ()
  {
    int size = lastBlock - firstBlock;
    return String.format ("%03d  %s  %-15s", size, parent.fileTypes[fileType], name);
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
}