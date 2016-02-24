package com.bytezone.diskbrowser.pascal;

import java.awt.Color;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.*;
import com.bytezone.diskbrowser.disk.*;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class PascalDisk extends AbstractFormattedDisk
{
  static final int CATALOG_ENTRY_SIZE = 26;
  private static DateFormat df = DateFormat.getDateInstance (DateFormat.SHORT);
  private final VolumeEntry volume;
  private final PascalCatalogSector diskCatalogSector;

  private final String[] fileTypes = { "Volume", "Xdsk", "Code", "Text", "Info", "Data",
                                      "Graf", "Foto", "SecureDir" };

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

    List<DiskAddress> blocks = disk.getDiskAddressList (0, 1);
    byte[] buffer = disk.readSectors (blocks);
    this.bootSector = new BootSector (disk, buffer, "Pascal");

    buffer = disk.readSector (2);
    byte[] data = new byte[CATALOG_ENTRY_SIZE];
    System.arraycopy (buffer, 0, data, 0, CATALOG_ENTRY_SIZE);

    volume = new VolumeEntry (data);

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
    diskCatalogSector = new PascalCatalogSector (disk, buffer); // uses all 4 sectors

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
      FileEntry fe = new FileEntry (data);
      fileEntries.add (fe);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode (fe);
      if (fe.fileType == 2) // PascalCode
      {
        node.setAllowsChildren (true);
        PascalCode pc = (PascalCode) fe.getDataSource ();
        for (PascalSegment ps : pc)
        {
          //					List<DiskAddress> blocks = new ArrayList<DiskAddress> ();
          DefaultMutableTreeNode segmentNode =
                new DefaultMutableTreeNode (new PascalCodeObject (ps, fe.firstBlock));
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
      return false; // will only work for floppies!

    List<DiskAddress> addresses = new ArrayList<DiskAddress> ();
    for (int i = 2; i < to; i++)
      addresses.add (disk.getDiskAddress (i));
    buffer = disk.readSectors (addresses);

    int blocks = HexFormatter.intValue (buffer[14], buffer[15]);
    if (blocks > 280)
      return false;
    int files = HexFormatter.intValue (buffer[16], buffer[17]);
    if (files < 0 || files > 77)
      return false;

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
      return new DefaultSector (name, disk, disk.readSector (da));
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
    String line = "----   ---------------   ----   --------  -------   ----   ----" + newLine;
    String date = volume.date == null ? "--" : df.format (volume.date.getTime ());
    StringBuilder text = new StringBuilder ();
    text.append ("Disk : " + disk.getFile ().getAbsolutePath () + newLine2);
    text.append ("Volume : " + volume.name + newLine);
    text.append ("Date   : " + date + newLine2);
    text.append ("Blks   Name              Type     Date     Length   Frst   Last" + newLine);
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
                                  ce.name, fileTypes[ce.fileType], date, bytes, ce.firstBlock,
                                  ce.lastBlock));
    }
    text.append (line);
    text.append (String.format ("Blocks free : %3d  Blocks used : %3d  Total blocks : %3d%n",
                                (volume.totalBlocks - usedBlocks), usedBlocks,
                                volume.totalBlocks));
    return new DefaultAppleFileSource (volume.name, text.toString (), this);
  }

  private abstract class CatalogEntry implements AppleFileSource
  {
    String name;
    int firstBlock;
    int lastBlock; // block AFTER last used block
    int fileType;
    GregorianCalendar date;
    List<DiskAddress> blocks = new ArrayList<DiskAddress> ();
    AbstractFile file;

    public CatalogEntry (byte[] buffer)
    {
      firstBlock = HexFormatter.intValue (buffer[0], buffer[1]);
      lastBlock = HexFormatter.intValue (buffer[2], buffer[3]);
      //			fileType = HexFormatter.intValue (buffer[4], buffer[5]);
      fileType = buffer[4] & 0x0F;
      name = HexFormatter.getPascalString (buffer, 6);

      for (int i = firstBlock; i < lastBlock; i++)
        blocks.add (disk.getDiskAddress (i));
    }

    private boolean contains (DiskAddress da)
    {
      for (DiskAddress sector : blocks)
        if (sector.compareTo (da) == 0)
          return true;
      return false;
    }

    @Override
    public String toString ()
    {
      int size = lastBlock - firstBlock;
      return String.format ("%03d  %s  %-15s", size, fileTypes[fileType], name);
    }

    @Override
    public List<DiskAddress> getSectors ()
    {
      List<DiskAddress> sectors = new ArrayList<DiskAddress> (blocks);
      return sectors;
    }

    @Override
    public FormattedDisk getFormattedDisk ()
    {
      return PascalDisk.this;
    }

    @Override
    public String getUniqueName ()
    {
      return name;
    }
  }

  private class VolumeEntry extends CatalogEntry
  {
    int totalFiles;
    int totalBlocks;

    public VolumeEntry (byte[] buffer)
    {
      super (buffer);
      totalBlocks = HexFormatter.intValue (buffer[14], buffer[15]);
      totalFiles = HexFormatter.intValue (buffer[16], buffer[17]);
      firstBlock = HexFormatter.intValue (buffer[18], buffer[19]);
      date = HexFormatter.getPascalDate (buffer, 20);

      //      for (int i = firstBlock; i < lastBlock; i++)
      //        sectorType[i] = catalogSector;
    }

    @Override
    public AbstractFile getDataSource ()
    {
      System.out.println ("in Volume Entry **********************");
      if (file != null)
        return file;

      byte[] buffer = disk.readSectors (blocks);
      file = new DefaultAppleFile (name, buffer);
      return file;
    }
  }

  private class FileEntry extends CatalogEntry
  {
    int bytesUsedInLastBlock;

    public FileEntry (byte[] buffer)
    {
      super (buffer);
      bytesUsedInLastBlock = HexFormatter.intValue (buffer[22], buffer[23]);
      date = HexFormatter.getPascalDate (buffer, 24);

      for (int i = firstBlock; i < lastBlock; i++)
        switch (fileType)
        {
          case 2:
            sectorTypes[i] = codeSector;
            break;
          case 3:
            sectorTypes[i] = textSector;
            break;
          case 4:
            sectorTypes[i] = infoSector;
            break;
          case 5:
            sectorTypes[i] = dataSector;
            break;
          case 6:
            sectorTypes[i] = grafSector;
            break;
          case 7:
            sectorTypes[i] = fotoSector;
            break;
          default:
            System.out.println ("Unknown pascal file type : " + fileType);
            sectorTypes[i] = dataSector;
            break;
        }
    }

    @Override
    public AbstractFile getDataSource ()
    {
      if (file != null)
        return file;

      byte[] buffer = getExactBuffer ();

      //      try
      {
        switch (fileType)
        {
          case 3:
            file = new PascalText (name, buffer);
            break;
          case 2:
            file = new PascalCode (name, buffer);
            break;
          case 4:
            file = new PascalInfo (name, buffer);
            break;
          case 0:
            // volume
            break;
          case 5:
            // data
            if (name.equals ("SYSTEM.CHARSET"))
            {
              file = new Charset (name, buffer);
              break;
            }
            if (name.equals ("WT")) // only testing
            {
              file = new WizardryTitle (name, buffer);
              break;
            }
            // intentional fall-through
          default:
            // unknown
            file = new DefaultAppleFile (name, buffer);
        }
      }
      //      catch (Exception e)
      //      {
      //        file = new ErrorMessageFile (name, buffer, e);
      //        e.printStackTrace ();
      //      }
      return file;
    }

    private byte[] getExactBuffer ()
    {
      byte[] buffer = disk.readSectors (blocks);
      byte[] exactBuffer;
      if (bytesUsedInLastBlock < 512)
      {
        int exactLength = buffer.length - 512 + bytesUsedInLastBlock;
        exactBuffer = new byte[exactLength];
        System.arraycopy (buffer, 0, exactBuffer, 0, exactLength);
      }
      else
        exactBuffer = buffer;
      return exactBuffer;
    }
  }

  class PascalCodeObject implements AppleFileSource
  {
    private final AbstractFile segment;
    private final List<DiskAddress> blocks;

    public PascalCodeObject (PascalSegment segment, int firstBlock)
    {
      this.segment = segment;
      this.blocks = new ArrayList<DiskAddress> ();

      int lo = firstBlock + segment.blockNo;
      int hi = lo + (segment.size - 1) / 512;
      for (int i = lo; i <= hi; i++)
        blocks.add (disk.getDiskAddress (i));
    }

    @Override
    public DataSource getDataSource ()
    {
      return segment;
    }

    @Override
    public FormattedDisk getFormattedDisk ()
    {
      return PascalDisk.this;
    }

    @Override
    public List<DiskAddress> getSectors ()
    {
      return blocks;
    }

    @Override
    public String getUniqueName ()
    {
      return segment.name; // this should be fileName/segmentName
    }

    @Override
    public String toString ()
    {
      return segment.name;
    }
  }
}