package com.bytezone.diskbrowser.pascal;

import java.awt.Color;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.BootSector;
import com.bytezone.diskbrowser.applefile.PascalCode;
import com.bytezone.diskbrowser.applefile.PascalSegment;
import com.bytezone.diskbrowser.disk.*;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class PascalDisk extends AbstractFormattedDisk
{
  static final int CATALOG_ENTRY_SIZE = 26;
  private final DateFormat df = DateFormat.getDateInstance (DateFormat.SHORT);
  private final VolumeEntry volume;
  private final PascalCatalogSector diskCatalogSector;

  final String[] fileTypes =
      { "Volume", "Xdsk", "Code", "Text", "Info", "Data", "Graf", "Foto", "SecureDir" };

  SectorType diskBootSector = new SectorType ("Boot", Color.lightGray);
  SectorType catalogSector = new SectorType ("Catalog", Color.magenta);
  SectorType dataSector = new SectorType ("Data", new Color (0, 200, 0)); // green
  SectorType codeSector = new SectorType ("Code", Color.red);
  SectorType textSector = new SectorType ("Text", Color.blue);
  SectorType infoSector = new SectorType ("Info", Color.orange);
  SectorType grafSector = new SectorType ("Graf", Color.cyan);
  SectorType fotoSector = new SectorType ("Foto", Color.gray);

  public PascalDisk (Disk disk)
  {
    super (disk);

    sectorTypesList.add (diskBootSector);
    sectorTypesList.add (catalogSector);
    sectorTypesList.add (dataSector);
    sectorTypesList.add (codeSector);
    sectorTypesList.add (textSector);
    sectorTypesList.add (infoSector);
    sectorTypesList.add (grafSector);
    sectorTypesList.add (fotoSector);

    //    DiskAddress da = disk.getDiskAddress (0);
    List<DiskAddress> blocks = disk.getDiskAddressList (0, 1);
    byte[] buffer = disk.readSectors (blocks);
    this.bootSector = new BootSector (disk, buffer, "Pascal", blocks);

    buffer = disk.readSector (2);
    byte[] data = new byte[CATALOG_ENTRY_SIZE];
    System.arraycopy (buffer, 0, data, 0, CATALOG_ENTRY_SIZE);

    volume = new VolumeEntry (this, data);

    for (int i = 0; i < 2; i++)
      if (!disk.isSectorEmpty (i))
      {
        sectorTypes[i] = diskBootSector;
        freeBlocks.set (i, false);
      }

    for (int i = 2; i < 280; i++)
      freeBlocks.set (i, true);

    List<DiskAddress> sectors = new ArrayList<DiskAddress> ();
    for (int i = 2; i < volume.lastBlock; i++)
    {
      DiskAddress da = disk.getDiskAddress (i);
      if (!disk.isSectorEmpty (da))
        sectorTypes[i] = catalogSector;
      sectors.add (da);
      freeBlocks.set (i, false);
    }

    buffer = disk.readSectors (sectors);
    diskCatalogSector = new PascalCatalogSector (disk, buffer, sectors);

    DefaultMutableTreeNode root = getCatalogTreeRoot ();
    DefaultMutableTreeNode volumeNode = new DefaultMutableTreeNode (volume);
    root.add (volumeNode);

    // read the catalog
    List<DiskAddress> addresses = new ArrayList<DiskAddress> ();
    for (int i = 2; i < volume.lastBlock; i++)
      addresses.add (disk.getDiskAddress (i));
    buffer = disk.readSectors (addresses);

    // loop through each catalog entry (what if there are deleted files?)
    for (int i = 1; i <= volume.totalFiles; i++)
    {
      int ptr = i * CATALOG_ENTRY_SIZE;
      data = new byte[CATALOG_ENTRY_SIZE];

      System.arraycopy (buffer, ptr, data, 0, CATALOG_ENTRY_SIZE);
      FileEntry fe = new FileEntry (this, data);
      fileEntries.add (fe);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode (fe);

      if (fe.fileType == 2)                   // PascalCode
      {
        node.setAllowsChildren (true);
        PascalCode pc = (PascalCode) fe.getDataSource ();
        for (PascalSegment ps : pc)
        {
          DefaultMutableTreeNode segmentNode =
              new DefaultMutableTreeNode (new PascalCodeObject (this, ps, fe.firstBlock));
          node.add (segmentNode);
          segmentNode.setAllowsChildren (false);
        }
      }
      else
        node.setAllowsChildren (false);

      volumeNode.add (node);
      for (int j = fe.firstBlock; j < fe.lastBlock; j++)
        freeBlocks.set (j, false);
    }

    volumeNode.setUserObject (getCatalog ());
    makeNodeVisible (volumeNode.getFirstLeaf ());
  }

  public static boolean isCorrectFormat (AppleDisk disk, boolean debug)
  {
    disk.setInterleave (1);
    if (checkFormat (disk, debug))
      return true;
    disk.setInterleave (0);
    if (checkFormat (disk, debug))
      return true;
    disk.setInterleave (3);
    return checkFormat (disk, debug);
  }

  public static boolean checkFormat (AppleDisk disk, boolean debug)
  {
    byte[] buffer = disk.readSector (2);
    if (debug)
      System.out.println (HexFormatter.format (buffer));
    int nameLength = HexFormatter.intValue (buffer[6]);
    if (nameLength < 1 || nameLength > 7)
    {
      if (debug)
        System.out.println ("bad name length : " + nameLength);
      return false;
    }

    if (debug)
    {
      String name = HexFormatter.getPascalString (buffer, 6);
      System.out.println ("Name ok : " + name);
    }

    int from = HexFormatter.intValue (buffer[0], buffer[1]);
    int to = HexFormatter.intValue (buffer[2], buffer[3]);
    if (from != 0 || to != 6)
    {
      if (debug)
        System.out.printf ("from: %d, to: %d%n", from, to);
      return false;                         // will only work for floppies!
    }

    List<DiskAddress> addresses = new ArrayList<DiskAddress> ();
    for (int i = 2; i < to; i++)
      addresses.add (disk.getDiskAddress (i));
    buffer = disk.readSectors (addresses);

    int files = HexFormatter.intValue (buffer[16], buffer[17]);
    if (files < 0 || files > 77)
    {
      if (debug)
        System.out.printf ("Files: %d%n", files);
      return false;
    }

    if (debug)
      System.out.println ("Files found : " + files);

    for (int i = 1; i <= files; i++)
    {
      int ptr = i * 26;
      int a = HexFormatter.intValue (buffer[ptr], buffer[ptr + 1]);
      int b = HexFormatter.intValue (buffer[ptr + 2], buffer[ptr + 3]);
      int c = HexFormatter.intValue (buffer[ptr + 4], buffer[ptr + 5]);
      if (b < a)
        return false;
      if (c == 0)
        return false;
      nameLength = HexFormatter.intValue (buffer[ptr + 6]);
      if (nameLength < 1 || nameLength > 15)
        return false;
      if (debug)
        System.out.printf ("%4d  %4d  %d  %s%n", a, b, c,
                           new String (buffer, ptr + 7, nameLength));
    }

    int blocks = HexFormatter.intValue (buffer[14], buffer[15]);
    if (blocks > 280)
    {
      if (debug)
        System.out.printf ("Blocks: %d%n", blocks);
      return false;
    }

    return true;
  }

  @Override
  public DataSource getFormattedSector (DiskAddress da)
  {
    SectorType st = sectorTypes[da.getBlock ()];
    if (st == diskBootSector)
      return bootSector;
    if (st == catalogSector)
      return diskCatalogSector;
    String name = getSectorFilename (da);
    if (name != null)
      return new DefaultSector (name, disk, disk.readSector (da), da);
    return super.getFormattedSector (da);
  }

  @Override
  public String getSectorFilename (DiskAddress da)
  {
    for (AppleFileSource ce : fileEntries)
      if (((CatalogEntry) ce).contains (da))
        return ((CatalogEntry) ce).name;
    return null;
  }

  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  {
    if (fileNo < 0 || fileNo >= fileEntries.size ())
      return null;
    return fileEntries.get (fileNo).getSectors ();
  }

  public DataSource getFile (int fileNo)
  {
    if (fileNo < 0 || fileNo >= fileEntries.size ())
      return null;
    return fileEntries.get (fileNo).getDataSource ();
  }

  @Override
  public AppleFileSource getCatalog ()
  {
    String newLine = String.format ("%n");
    String newLine2 = newLine + newLine;
    String line =
        "----   ---------------   ----   --------  -------   ----   ----" + newLine;
    String date = volume.date == null ? "--" : df.format (volume.date.getTime ());
    StringBuilder text = new StringBuilder ();
    text.append ("Disk : " + disk.getFile ().getAbsolutePath () + newLine2);
    text.append ("Volume : " + volume.name + newLine);
    text.append ("Date   : " + date + newLine2);
    text.append ("Blks   Name              Type     Date     Length   Frst   Last"
        + newLine);
    text.append (line);

    int usedBlocks = 6;
    for (AppleFileSource fe : fileEntries)
    {
      FileEntry ce = (FileEntry) fe;
      int size = ce.lastBlock - ce.firstBlock;
      usedBlocks += size;
      date = ce.date == null ? "--" : df.format (ce.date.getTime ());
      int bytes = (size - 1) * 512 + ce.bytesUsedInLastBlock;
      text.append (String.format (" %3d   %-15s   %s   %8s %,8d   $%03X   $%03X%n", size,
                                  ce.name, fileTypes[ce.fileType], date, bytes,
                                  ce.firstBlock, ce.lastBlock));
    }
    text.append (line);
    text.append (String
        .format ("Blocks free : %3d  Blocks used : %3d  Total blocks : %3d%n",
                 (volume.totalBlocks - usedBlocks), usedBlocks, volume.totalBlocks));
    return new DefaultAppleFileSource (volume.name, text.toString (), this);
  }
}