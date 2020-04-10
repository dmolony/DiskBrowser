package com.bytezone.diskbrowser.pascal;

import java.awt.Color;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.BootSector;
import com.bytezone.diskbrowser.disk.AbstractFormattedDisk;
import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.DefaultSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.SectorType;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.wizardry.Relocator;

// -----------------------------------------------------------------------------------//
public class PascalDisk extends AbstractFormattedDisk
// -----------------------------------------------------------------------------------//
{
  static final int CATALOG_ENTRY_SIZE = 26;
  private final DateFormat df = DateFormat.getDateInstance (DateFormat.SHORT);
  private final VolumeEntry volumeEntry;
  private final PascalCatalogSector diskCatalogSector;

  protected Relocator relocator;

  final String[] fileTypes =
      { "Volume", "Bad ", "Code", "Text", "Info", "Data", "Graf", "Foto", "SecureDir" };

  SectorType diskBootSector = new SectorType ("Boot", Color.lightGray);
  SectorType catalogSector = new SectorType ("Catalog", Color.magenta);
  SectorType dataSector = new SectorType ("Data", new Color (0, 200, 0)); // green
  SectorType codeSector = new SectorType ("Code", Color.red);
  SectorType textSector = new SectorType ("Text", Color.blue);
  SectorType infoSector = new SectorType ("Info", Color.orange);
  SectorType grafSector = new SectorType ("Graf", Color.cyan);
  SectorType fotoSector = new SectorType ("Foto", Color.gray);
  SectorType badSector = new SectorType ("Bad", Color.darkGray);

  SectorType[] sectors = { catalogSector, badSector, codeSector, textSector, infoSector,
                           dataSector, grafSector, fotoSector };

  // ---------------------------------------------------------------------------------//
  public PascalDisk (Disk disk)
  // ---------------------------------------------------------------------------------//
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
    sectorTypesList.add (badSector);

    List<DiskAddress> blocks = disk.getDiskAddressList (0, 1);    // B0, B1
    this.bootSector = new BootSector (disk, disk.readBlocks (blocks), "Pascal");

    for (int i = 0; i < 2; i++)
      if (!disk.isBlockEmpty (i))
      {
        sectorTypes[i] = diskBootSector;
        freeBlocks.set (i, false);
      }

    for (int i = 2; i < disk.getTotalBlocks (); i++)
      freeBlocks.set (i, true);

    byte[] buffer = disk.readBlock (2);
    byte[] data = new byte[CATALOG_ENTRY_SIZE];
    System.arraycopy (buffer, 0, data, 0, CATALOG_ENTRY_SIZE);
    volumeEntry = new VolumeEntry (this, data);

    DefaultMutableTreeNode root = getCatalogTreeRoot ();
    DefaultMutableTreeNode volumeNode = new DefaultMutableTreeNode (volumeEntry);
    root.add (volumeNode);

    List<DiskAddress> sectors = new ArrayList<> ();
    int max = Math.min (volumeEntry.lastBlock, disk.getTotalBlocks ());
    for (int i = 2; i < max; i++)
    {
      DiskAddress da = disk.getDiskAddress (i);
      if (!disk.isBlockEmpty (da))
        sectorTypes[i] = catalogSector;
      sectors.add (da);
      freeBlocks.set (i, false);
    }

    diskCatalogSector =
        new PascalCatalogSector (disk, disk.readBlocks (sectors), sectors);

    // read the catalog
    List<DiskAddress> addresses = new ArrayList<> ();
    for (int i = 2; i < max; i++)
      addresses.add (disk.getDiskAddress (i));
    buffer = disk.readBlocks (addresses);

    // loop through each catalog entry (what if there are deleted files?)
    for (int i = 1; i <= volumeEntry.totalFiles; i++)
    {
      int ptr = i * CATALOG_ENTRY_SIZE;
      data = new byte[CATALOG_ENTRY_SIZE];
      System.arraycopy (buffer, ptr, data, 0, CATALOG_ENTRY_SIZE);
      FileEntry fileEntry = new FileEntry (this, data);

      fileEntries.add (fileEntry);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode (fileEntry);
      fileEntry.setNode (node);

      if (fileEntry.fileType == 2)
      {
        node.setAllowsChildren (true);
        fileEntry.getDataSource ();         // build segments
      }
      else
        node.setAllowsChildren (false);

      volumeNode.add (node);
      for (int j = fileEntry.firstBlock; j < fileEntry.lastBlock; j++)
        freeBlocks.set (j, false);
    }

    volumeNode.setUserObject (getCatalog ());
    makeNodeVisible (volumeNode.getFirstLeaf ());
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isCorrectFormat (AppleDisk disk, boolean debug)
  // ---------------------------------------------------------------------------------//
  {
    disk.setInterleave (1);                 // should only ever be Prodos
    if (checkFormat (disk, debug))
      return true;
    disk.setInterleave (0);                 // see SANE Disk 2.po
    if (checkFormat (disk, debug))
      return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean checkFormat (AppleDisk disk, boolean debug)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = disk.readBlock (2);
    int nameLength = buffer[6] & 0xFF;
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

    int blocks = HexFormatter.intValue (buffer[14], buffer[15]);
    if (blocks > 280)
    {
      if (debug)
        System.out.printf ("Blocks > 280: %d%n", blocks);
      //      return false;
    }

    List<DiskAddress> addresses = new ArrayList<> ();
    for (int i = 2; i < to; i++)
      addresses.add (disk.getDiskAddress (i));
    buffer = disk.readBlocks (addresses);

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
      int firstBlock = HexFormatter.intValue (buffer[ptr], buffer[ptr + 1]);
      int lastBlock = HexFormatter.intValue (buffer[ptr + 2], buffer[ptr + 3]);
      int kind = HexFormatter.intValue (buffer[ptr + 4], buffer[ptr + 5]);
      if (lastBlock < firstBlock)
        return false;
      if (kind == 0)
        return false;
      nameLength = buffer[ptr + 6] & 0xFF;
      if (nameLength < 1 || nameLength > 15)
        return false;
      int lastByte = HexFormatter.intValue (buffer[ptr + 22], buffer[ptr + 23]);
      GregorianCalendar date = HexFormatter.getPascalDate (buffer, 24);
      if (debug)
        System.out.printf ("%4d  %4d  %d  %-15s %d %s%n", firstBlock, lastBlock, kind,
            new String (buffer, ptr + 7, nameLength), lastByte, date);
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getFormattedSector (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    SectorType st = sectorTypes[da.getBlockNo ()];
    if (st == diskBootSector)
      return bootSector;
    if (st == catalogSector)
      return diskCatalogSector;
    String name = getSectorFilename (da);
    if (name != null)
      return new DefaultSector (name, disk, disk.readBlock (da), da);
    return super.getFormattedSector (da);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getSectorFilename (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    for (AppleFileSource ce : fileEntries)
      if (((CatalogEntry) ce).contains (da))
        return ((CatalogEntry) ce).name;
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  // ---------------------------------------------------------------------------------//
  {
    if (fileNo < 0 || fileNo >= fileEntries.size ())
      return null;
    return fileEntries.get (fileNo).getSectors ();
  }

  // ---------------------------------------------------------------------------------//
  public DataSource getFile (int fileNo)
  // ---------------------------------------------------------------------------------//
  {
    if (fileNo < 0 || fileNo >= fileEntries.size ())
      return null;
    return fileEntries.get (fileNo).getDataSource ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AppleFileSource getCatalog ()
  // ---------------------------------------------------------------------------------//
  {
    String newLine = String.format ("%n");
    String newLine2 = newLine + newLine;
    String line = "----   ---------------   ----   --------  -------   ----   ----   ----"
        + newLine;
    String date =
        volumeEntry.date == null ? "--" : df.format (volumeEntry.date.getTime ());
    StringBuilder text = new StringBuilder ();
    text.append ("Disk : " + getDisplayPath () + newLine2);
    text.append ("Volume : " + volumeEntry.name + newLine);
    text.append ("Date   : " + date + newLine2);
    text.append (
        "Blks   Name              Type     Date     Length   Frst   Last   Blks\n");
    text.append (line);

    int usedBlocks = 6;
    for (AppleFileSource fe : fileEntries)
    {
      FileEntry ce = (FileEntry) fe;
      int size = ce.lastBlock - ce.firstBlock;
      usedBlocks += size;
      date = ce.date == null ? "--" : df.format (ce.date.getTime ());
      int bytes = (size - 1) * 512 + ce.bytesUsedInLastBlock;
      String fileType = ce.fileType < 0 || ce.fileType >= fileTypes.length ? "????"
          : fileTypes[ce.fileType];
      text.append (String.format ("%4d   %-15s   %s   %8s %,8d   $%03X   $%03X   $%03X%n",
          size, ce.name, fileType, date, bytes, ce.firstBlock, ce.lastBlock, size));
    }
    text.append (line);
    text.append (
        String.format ("Blocks free : %3d  Blocks used : %3d  Total blocks : %3d%n",
            (volumeEntry.totalBlocks - usedBlocks), usedBlocks, volumeEntry.totalBlocks));
    return new DefaultAppleFileSource (volumeEntry.name, text.toString (), this);
  }
}