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
  public final SectorType otherSector = new SectorType ("Other", Color.blue);
  public final SectorType docSector = new SectorType ("DOC", Color.cyan);
  public final SectorType basSector = new SectorType ("BAS", Color.gray);
  public final SectorType asmSector = new SectorType ("ASM", Color.orange);
  public final SectorType ovrSector = new SectorType ("OVR", Color.magenta);
  public final SectorType macSector = new SectorType ("MAC", Color.green);

  private int version;      // http://www.seasip.info/Cpm/format22.html

  public CPMDisk (Disk disk)
  {
    super (disk);

    sectorTypesList.add (catalogSector);
    sectorTypesList.add (prnSector);
    sectorTypesList.add (comSector);
    sectorTypesList.add (basSector);
    sectorTypesList.add (docSector);
    sectorTypesList.add (asmSector);
    sectorTypesList.add (ovrSector);
    sectorTypesList.add (macSector);
    sectorTypesList.add (otherSector);

    setEmptyByte ((byte) 0xE5);

    // search for the version string
    for (int i = 8; i >= 4; i -= 2)
    {
      byte[] buffer = disk.readSector (0, i);
      String text = new String (buffer, 16, 24);
      if ("DIR ERA TYPESAVEREN USER".equals (text))
      {
        version = buffer[41] & 0xFF;
        break;
      }
    }

    DefaultMutableTreeNode root = getCatalogTreeRoot ();

    for (int sector = 0; sector < 8; sector++)
    {
      DiskAddress da = disk.getDiskAddress (3, sector);

      sectorTypes[da.getBlock ()] = catalogSector;
      byte[] buffer = disk.readSector (da);
      int b1 = buffer[0] & 0xFF;
      int b2 = buffer[1] & 0xFF;
      if (b1 == 0xE5)
        continue;
      if (b1 > 31)
        break;
      if (b2 < 32 || (b2 > 126 && b2 != 0xE5))
        break;

      for (int i = 0; i < buffer.length; i += 32)
      {
        b1 = buffer[i] & 0xFF;
        b2 = buffer[i + 1] & 0xFF;
        if (b1 == 0xE5)
          break;
        if (b2 < 32 || (b2 > 126 && b2 != 0xE5))
          break;

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
    if ("OVR".equals (type))
      return ovrSector;
    if ("MAC".equals (type))
      return macSector;

    return otherSector;
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
      return new CPMCatalogSector (disk, buffer, da);

    return super.getFormattedSector (da);
  }

  @Override
  public AppleFileSource getCatalog ()
  {
    String newLine = String.format ("%n");
    String line =
        "----  ---------  --- - -  --   --   --   --   ----------------------------"
            + "-------------------" + newLine;
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Disk : %s%n%n", getAbsolutePath ()));
    text.append ("User  Name       Typ R S  Ex   S2   S1   RC   Blocks" + newLine);
    text.append (line);

    for (AppleFileSource entry : fileEntries)
    {
      text.append (((DirectoryEntry) entry).line ());
      text.append (newLine);
    }
    text.append (line);
    if (version != 0)
      text.append ("Version: " + version);

    return new DefaultAppleFileSource ("CPM Disk ", text.toString (), this);
  }

  public static boolean isCorrectFormat (AppleDisk disk)
  {
    disk.setInterleave (3);

    for (int i = 8; i >= 4; i -= 2)
    {
      byte[] buffer = disk.readSector (0, i);
      String text = new String (buffer, 16, 24);
      if ("DIR ERA TYPESAVEREN USER".equals (text))
      {
        int version = buffer[41] & 0xFF;
        System.out.printf ("CPM version %d%n", version);
        return true;
      }
    }

    for (int sector = 0; sector < 8; sector++)
    {
      byte[] buffer = disk.readSector (3, sector);

      // check if entire sector is empty (everything == 0xE5)
      if (bufferContainsAll (buffer, (byte) 0xE5))
        break;

      for (int i = 0; i < buffer.length; i += 32)
      {
        int val = buffer[i] & 0xFF;
        if (val == 0xE5)
          break;

        if (val > 31)   // && val != 0xE5)
          return false;

        for (int j = 1; j <= 8; j++)
        {
          val = buffer[i + j] & 0xFF;
          if (val < 32 || (val > 126 && val != 0xE5))
            return false;
        }
      }
    }

    return true;
  }

  private static boolean bufferContainsAll (byte[] buffer, byte value)
  {
    for (byte b : buffer)
      if (b != value)
        return false;
    return true;
  }
}