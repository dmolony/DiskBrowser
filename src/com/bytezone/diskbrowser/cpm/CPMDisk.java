package com.bytezone.diskbrowser.cpm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.AbstractFormattedDisk;
import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.SectorType;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class CPMDisk extends AbstractFormattedDisk
// -----------------------------------------------------------------------------------//
{
  private static final int EMPTY_BYTE_VALUE = 0xE5;

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
  //                        // http://www.seasip.info/Cpm/format31.html
  private final DefaultMutableTreeNode volumeNode;

  // ---------------------------------------------------------------------------------//
  public CPMDisk (Disk disk)
  // ---------------------------------------------------------------------------------//
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

    setEmptyByte ((byte) EMPTY_BYTE_VALUE);

    // search for the version string
    for (int i = 8; i >= 4; i -= 2)
    {
      byte[] buffer = disk.readBlock (0, i);
      String text = new String (buffer, 16, 24);
      if ("DIR ERA TYPESAVEREN USER".equals (text))
      {
        version = buffer[41] & 0xFF;
        break;
      }
    }

    DefaultMutableTreeNode rootNode = getCatalogTreeRoot ();
    volumeNode = new DefaultMutableTreeNode ();
    rootNode.add (volumeNode);

    for (int sector = 0; sector < 8; sector++)
    {
      DiskAddress da = disk.getDiskAddress (3, sector);

      sectorTypes[da.getBlockNo ()] = catalogSector;
      byte[] buffer = disk.readBlock (da);

      int b1 = buffer[0] & 0xFF;
      int b2 = buffer[1] & 0xFF;

      if (b1 == EMPTY_BYTE_VALUE && (b2 == EMPTY_BYTE_VALUE || b2 == 0))
        continue;

      if (b1 > 31 && b1 != EMPTY_BYTE_VALUE)
        break;

      if (b2 <= 32 || (b2 > 126 && b2 != EMPTY_BYTE_VALUE))
        break;

      for (int i = 0; i < buffer.length; i += 32)
      {
        b1 = buffer[i] & 0xFF;
        b2 = buffer[i + 1] & 0xFF;

        if (b1 == EMPTY_BYTE_VALUE)         // deleted file??
          continue;

        if (b2 <= 32 || (b2 > 126 && b2 != EMPTY_BYTE_VALUE))
          break;

        DirectoryEntry entry = new DirectoryEntry (this, buffer, i);
        SectorType sectorType = getSectorType (entry.getType ());
        for (DiskAddress block : entry.getSectors ())
          if (!disk.isBlockEmpty (block))
            sectorTypes[block.getBlockNo ()] = sectorType;

        DirectoryEntry parent = findParent (entry);
        if (parent == null)
        {
          fileEntries.add (entry);
          DefaultMutableTreeNode node = new DefaultMutableTreeNode (entry);
          volumeNode.add (node);
          node.setAllowsChildren (false);
        }
        else
          parent.add (entry);
      }
    }

    volumeNode.setUserObject (getCatalog ());
    makeNodeVisible (volumeNode.getFirstLeaf ());
  }

  // ---------------------------------------------------------------------------------//
  private SectorType getSectorType (String type)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  // ---------------------------------------------------------------------------------//
  {
    if (fileEntries.size () > 0 && fileEntries.size () > fileNo)
      return fileEntries.get (fileNo).getSectors ();

    return null;
  }

  // ---------------------------------------------------------------------------------//
  private DirectoryEntry findParent (DirectoryEntry child)
  // ---------------------------------------------------------------------------------//
  {
    for (AppleFileSource entry : fileEntries)
      if (((DirectoryEntry) entry).matches (child))
        return (DirectoryEntry) entry;

    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getFormattedSector (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    SectorType type = sectorTypes[da.getBlockNo ()];
    byte[] buffer = disk.readBlock (da);

    if (type == catalogSector)
      return new CPMCatalogSector (disk, buffer, da);

    return super.getFormattedSector (da);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AppleFileSource getCatalog ()
  // ---------------------------------------------------------------------------------//
  {
    String line = "----  ---------  --- - -  --   --   --   --   ----------------------------"
        + "-------------------\n";
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("File : %s%n%n", getDisplayPath ()));
    text.append ("User  Name       Typ R S  Ex   S2   S1   RC   Blocks\n");
    text.append (line);

    for (AppleFileSource entry : fileEntries)
    {
      text.append (((DirectoryEntry) entry).line ());
      text.append ("\n");
    }
    text.append (line);
    if (version != 0)
      text.append ("Version: " + version);

    return new DefaultAppleFileSource ("CPM Disk ", text.toString (), this);
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isCorrectFormat (AppleDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    boolean debug = false;

    disk.setInterleave (3);

    // collect catalog sectors
    List<DiskAddress> catalog = new ArrayList<> ();
    for (int i = 0; i < 8; i++)
      catalog.add (disk.getDiskAddress (3, i));
    byte[] buffer = disk.readBlocks (catalog);

    if (debug)
      System.out.println (HexFormatter.format (buffer));

    for (int i = 0; i < 2; i++)
    {
      int start = i * 1024;
      int end = start + 1024;

      for (int ptr = start; ptr < end; ptr += 32)
      {
        if (buffer[ptr] == (byte) EMPTY_BYTE_VALUE)
        {
          if (buffer[ptr + 1] == (byte) EMPTY_BYTE_VALUE      //
              || buffer[ptr + 1] == 0)                        // finished this block
            break;
          continue;                                           // deleted file?
        }

        int userNo = buffer[ptr] & 0xFF;
        if (userNo > 31)
          return false;

        for (int j = 1; j < 12; j++)
        {
          int ch = buffer[ptr + j] & 0x7F;                    // remove flag
          if (ch < 32 || ch > 126)                            // invalid ascii
            return false;
        }

        if (debug)
        {
          String fileName = new String (buffer, ptr + 1, 8);
          String fileType = new String (buffer, ptr + 9, 3);
          System.out.printf ("%2d  %s %s%n", userNo, fileName, fileType);
        }
      }
    }

    if (debug)
      System.out.println ("CP/M disk");

    return true;
  }

  // ---------------------------------------------------------------------------------//
  private static boolean isCorrectFormat2 (AppleDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    disk.setInterleave (3);

    for (int i = 8; i >= 4; i -= 2)
    {
      byte[] buffer = disk.readBlock (0, i);
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
      byte[] buffer = disk.readBlock (3, sector);
      System.out.println (HexFormatter.format (buffer));

      // check if entire sector is empty (everything == 0xE5)
      if (bufferContainsAll (buffer, (byte) EMPTY_BYTE_VALUE))
        break;

      int b1 = buffer[0] & 0xFF;
      int b2 = buffer[1] & 0xFF;

      if (b1 == EMPTY_BYTE_VALUE && (b2 == EMPTY_BYTE_VALUE || b2 == 0))
        continue;

      if (b1 > 31 && b1 != EMPTY_BYTE_VALUE)
        break;

      if (b2 < 32 || (b2 > 126 && b2 != EMPTY_BYTE_VALUE))
        break;

      for (int i = 0; i < buffer.length; i += 32)
      {
        b1 = buffer[i] & 0xFF;
        b2 = buffer[i + 1] & 0xFF;

        if (b1 == EMPTY_BYTE_VALUE)         // deleted file??
          continue;

        if (b2 < 32 || (b2 > 126 && b2 != EMPTY_BYTE_VALUE))
          return false;

        //        int val = buffer[i] & 0xFF;
        //        if (val == EMPTY_BYTE_VALUE)
        //        {
        //          if (debug)
        //            System.out.println ("empty value found - deleted file?");
        //          break;
        //        }

        //        if (val > 31)
        //        {
        //          if (debug)
        //            System.out.println ("val > 31");
        //          return false;
        //        }

        //        for (int j = 1; j <= 8; j++)
        //        {
        //          val = buffer[i + j] & 0xFF;
        //          if (val < 32 || (val > 126 && val != EMPTY_BYTE_VALUE))
        //          {
        //            if (debug)
        //              System.out.println ("val < 32 || val > 126");
        //            return false;
        //          }
        //        }
      }
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  private static boolean bufferContainsAll (byte[] buffer, byte value)
  // ---------------------------------------------------------------------------------//
  {
    for (byte b : buffer)
      if (b != value)
        return false;
    return true;
  }

  // ---------------------------------------------------------------------------------//
  //  @Override
  //  public String toString ()
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    StringBuffer text = new StringBuffer ("CPM disk");
  //    return text.toString ();
  //  }
}