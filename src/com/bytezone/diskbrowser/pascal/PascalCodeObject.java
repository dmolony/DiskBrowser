package com.bytezone.diskbrowser.pascal;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.PascalSegment;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.DataSource;

class PascalCodeObject implements AppleFileSource
{
  private final PascalDisk parent;
  private final AbstractFile segment;
  private final List<DiskAddress> blocks = new ArrayList<> ();

  public PascalCodeObject (PascalDisk parent, PascalSegment segment, int firstBlock)
  {
    this.parent = parent;
    this.segment = segment;

    int lo = firstBlock + segment.blockNo;
    int hi = lo + (segment.size - 1) / 512;
    Disk disk = parent.getDisk ();
    int max = Math.min (hi, 279);
    for (int i = lo; i <= max; i++)
      blocks.add (disk.getDiskAddress (i));
  }

  @Override
  public DataSource getDataSource ()
  {
    return segment;
  }

  @Override
  public FormattedDisk getFormattedDisk ()
  {
    return parent;
  }

  @Override
  public List<DiskAddress> getSectors ()
  {
    return blocks;
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
  public String getUniqueName ()
  {
    return segment.getName ();            // this should be fileName/segmentName
  }

  @Override
  public String toString ()
  {
    return segment.getName ();
  }
}