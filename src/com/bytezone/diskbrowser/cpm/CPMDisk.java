package com.bytezone.diskbrowser.cpm;

import java.awt.Color;
import java.util.ArrayList;
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
  private final List<DirectoryEntry> directoryEntries = new ArrayList<DirectoryEntry> ();

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

      buffer = disk.readSector (da);
      for (int i = 0; i < buffer.length; i += 32)
      {
        if (buffer[i] != 0 && buffer[i] != (byte) 0xE5)
          break;
        if (buffer[i] == 0)
        {
          DirectoryEntry entry = new DirectoryEntry (buffer, i);
          DirectoryEntry parent = findParent (entry);
          if (parent == null)
            directoryEntries.add (entry);
          else
            parent.add (entry);
        }
      }
    }
    listEntries ();
  }

  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  {
    return null;
  }

  public void listEntries ()
  {
    for (DirectoryEntry entry : directoryEntries)
      System.out.println (entry);
  }

  private DirectoryEntry findParent (DirectoryEntry child)
  {
    for (DirectoryEntry entry : directoryEntries)
    {
      if (entry.matches (child))
        return entry;
    }
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
        int val = buffer[i] & 0xFF;
        if (val > 31 && val != 0xE5)
          return false;
        //        if (buffer[i] == 0)
        //          System.out.println (new DirectoryEntry (buffer, i));
      }
    }

    return true;
  }
}