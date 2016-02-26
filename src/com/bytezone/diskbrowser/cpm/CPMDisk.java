package com.bytezone.diskbrowser.cpm;

import java.awt.Color;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.*;
import com.bytezone.diskbrowser.gui.DataSource;

public class CPMDisk extends AbstractFormattedDisk
{
  private final Color green = new Color (0, 200, 0);

  public final SectorType catalogSector = new SectorType ("Catalog", green);
  public final SectorType prnSector = new SectorType ("PRN", Color.lightGray);
  public final SectorType comSector = new SectorType ("COM", Color.red);
  public final SectorType dataSector = new SectorType ("Data", Color.blue);
  public final SectorType docSector = new SectorType ("DOC", Color.cyan);
  public final SectorType basSector = new SectorType ("BAS", Color.gray);
  public final SectorType asmSector = new SectorType ("ASM", Color.orange);

  private int version;      // http://www.seasip.info/Cpm/format22.html

  public CPMDisk (Disk disk)
  {
    super (disk);

    sectorTypesList.add (catalogSector);
    sectorTypesList.add (prnSector);
    sectorTypesList.add (comSector);
    sectorTypesList.add (dataSector);
    sectorTypesList.add (basSector);
    sectorTypesList.add (docSector);
    sectorTypesList.add (asmSector);

    getDisk ().setEmptyByte ((byte) 0xE5);
    setSectorTypes ();

    byte[] buffer = disk.readSector (0, 8);
    String text = new String (buffer, 16, 24);
    if ("DIR ERA TYPESAVEREN USER".equals (text))
      version = buffer[41] & 0xFF;

    DefaultMutableTreeNode root = getCatalogTreeRoot ();

    for (int sector = 0; sector < 8; sector++)
    {
      DiskAddress da = disk.getDiskAddress (3, sector);
      if (disk.isSectorEmpty (da))
        break;

      sectorTypes[da.getBlock ()] = catalogSector;
      buffer = disk.readSector (da);

      for (int i = 0; i < buffer.length; i += 32)
      {
        if (buffer[i] != 0 && buffer[i] != (byte) 0xE5)
          break;
        if (buffer[i] == 0)
        {
          DirectoryEntry entry = new DirectoryEntry (this, buffer, i);
          SectorType sectorType = getSectorType (entry.getType ());
          for (DiskAddress block : entry.getSectors ())
            if (!disk.isSectorEmpty (block))
              sectorTypes[block.getBlock ()] = sectorType;

          DirectoryEntry parent = findParent (entry);
          if (parent == null)
          {
            fileEntries.add (entry);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode (entry);
            root.add (node);
            node.setAllowsChildren (false);
          }
          else
            parent.add (entry);
        }
      }
    }

    root.setUserObject (getCatalog ());         // override the disk's default display
    makeNodeVisible (root.getFirstLeaf ());
  }

  private SectorType getSectorType (String type)
  {
    if ("COM".equals (type))
      return comSector;
    if ("DOC".equals (type))
      return docSector;
    if ("BAS".equals (type))
      return basSector;
    if ("PRN".equals (type))
      return prnSector;
    if ("ASM".equals (type))
      return asmSector;

    return dataSector;
  }

  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  {
    if (fileEntries.size () > 0 && fileEntries.size () > fileNo)
      return fileEntries.get (fileNo).getSectors ();
    return null;
  }

  private DirectoryEntry findParent (DirectoryEntry child)
  {
    for (AppleFileSource entry : fileEntries)
      if (((DirectoryEntry) entry).matches (child))
        return (DirectoryEntry) entry;

    return null;
  }

  @Override
  public DataSource getFormattedSector (DiskAddress da)
  {
    SectorType type = sectorTypes[da.getBlock ()];
    byte[] buffer = disk.readSector (da);

    if (type == catalogSector)
      return new CPMCatalogSector (disk, buffer);

    return super.getFormattedSector (da);
  }

  @Override
  public AppleFileSource getCatalog ()
  {
    String newLine = String.format ("%n");
    String line =
        "----  ---------  ---   --   --   --   --   ----------------------------"
            + "-------------------" + newLine;
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Disk : %s%n%n", getAbsolutePath ()));
    text.append ("User  Name       Typ   Ex   S2   S1   RC   Blocks" + newLine);
    text.append (line);

    for (AppleFileSource entry : fileEntries)
    {
      text.append (((DirectoryEntry) entry).line ());
      text.append (newLine);
    }
    text.append (line);

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