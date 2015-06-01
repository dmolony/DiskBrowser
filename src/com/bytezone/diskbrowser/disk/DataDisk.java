package com.bytezone.diskbrowser.disk;

import java.util.List;

import com.bytezone.diskbrowser.gui.DataSource;

public class DataDisk extends AbstractFormattedDisk
{
  //  static final byte[] dos = { 0x01, (byte) 0xA5, 0x27, (byte) 0xC9, 0x09 };

  // this should somehow tie in with the checksum from DiskFactory to determine
  // whether it has a bootloader

  public DataDisk (AppleDisk disk)
  {
    super (disk);

    //    byte[] buffer = disk.readSector (0, 0); // Boot sector
    //    boolean ok = true;
    //    for (int i = 0; i < dos.length; i++)
    //      if (buffer[i] != dos[i])
    //      {
    //        ok = false;
    //        break;
    //      }
    //    if (buffer[0] == 0x01)
    //    {
    //      bootSector = new BootSector (disk, buffer, "DOS");
    //      sectorTypesList.add (dosSector);
    //      sectorTypes[0] = dosSector;
    //    }
  }

  // no files on data disks
  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  {
    return null;
  }

  // no files on data disks
  public DataSource getFile (int fileNo)
  {
    return null;
  }

  @Override
  public String toString ()
  {
    return disk.toString ();
  }
}