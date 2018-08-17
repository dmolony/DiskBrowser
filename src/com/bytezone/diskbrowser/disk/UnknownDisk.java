package com.bytezone.diskbrowser.disk;

import java.util.ArrayList;
import java.util.List;

public class UnknownDisk extends AbstractFormattedDisk
{
  // could arrange for the blocks to appear as a question mark

  public UnknownDisk (AppleDisk disk)
  {
    super (disk);
  }

  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  {
    return new ArrayList<> ();
  }
}
