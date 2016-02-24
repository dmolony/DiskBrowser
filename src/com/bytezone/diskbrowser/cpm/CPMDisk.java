package com.bytezone.diskbrowser.cpm;

import java.awt.Color;
import java.util.List;

import com.bytezone.diskbrowser.applefile.BootSector;
import com.bytezone.diskbrowser.disk.AbstractFormattedDisk;
import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.SectorType;

public class CPMDisk extends AbstractFormattedDisk
{
  private final Color green = new Color (0, 200, 0);
  public final SectorType catalogSector = new SectorType ("Catalog", green);
  public final SectorType cpmSector = new SectorType ("CPM", Color.lightGray);

  private int version;      // http://www.seasip.info/Cpm/format22.html

  public CPMDisk (Disk disk)
  {
    super (disk);
    sectorTypesList.add (catalogSector);
    sectorTypesList.add (cpmSector);

    byte[] sectorBuffer = disk.readSector (0, 0); // Boot sector
    bootSector = new BootSector (disk, sectorBuffer, "CPM");
    sectorTypes[0] = cpmSector;

    byte[] buffer = disk.readSector (0, 8);
    String text = new String (buffer, 16, 24);
    if ("DIR ERA TYPESAVEREN USER".equals (text))
      version = buffer[41] & 0xFF;

    for (int sector = 0; sector < 8; sector++)
    {
      DiskAddress da = disk.getDiskAddress (3, sector);
      sectorTypes[da.getBlock ()] = catalogSector;
    }
  }

  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  {
    return null;
  }

  public static boolean isCorrectFormat (AppleDisk disk)
  {
    disk.setInterleave (3);

    byte[] buffer = disk.readSector (0, 8);
    String text = new String (buffer, 16, 24);
    System.out.println (text);
    if ("DIR ERA TYPESAVEREN USER".equals (text))
      return true;

    buffer = disk.readSector (0, 4);
    text = new String (buffer, 16, 24);
    System.out.println (text);
    if ("DIR ERA TYPESAVEREN USER".equals (text))
      return true;

    for (int sector = 0; sector < 8; sector++)
    {
      buffer = disk.readSector (3, sector);
      for (int i = 0; i < buffer.length; i += 32)
      {
        if (buffer[i] != 0 && buffer[i] != (byte) 0xE5)
          return false;
        if (buffer[i] == 0)
          System.out.println (new DirectoryEntry (buffer, i));
      }
    }

    return true;
  }
}