package com.bytezone.diskbrowser.cpm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.BootSector;
import com.bytezone.diskbrowser.disk.*;

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
    //    listEntries ();
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

  @Override
  public AppleFileSource getCatalog ()
  {
    String newLine = String.format ("%n");
    String line = "---  ---------  ----" + newLine;
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Disk : %s%n%n", getAbsolutePath ()));
    text.append ("User  Name    Type" + newLine);
    text.append (line);

    for (DirectoryEntry entry : directoryEntries)
    {
      text.append (entry.line ());
      text.append (newLine);
    }

    return new DefaultAppleFileSource ("CPM Disk ", text.toString (), this);
  }

  public static boolean isCorrectFormat (AppleDisk disk)
  {
    disk.setInterleave (3);

    byte[] buffer = disk.readSector (0, 8);
    String text = new String (buffer, 16, 24);
    if ("DIR ERA TYPESAVEREN USER".equals (text))
      return true;

    buffer = disk.readSector (0, 4);
    text = new String (buffer, 16, 24);
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
      }
    }

    return true;
  }
}