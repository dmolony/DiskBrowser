package com.bytezone.diskbrowser.prodos;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.BootSector;
import com.bytezone.diskbrowser.disk.*;
import com.bytezone.diskbrowser.gui.DataSource;

public class ProdosDisk extends AbstractFormattedDisk
{
  final SectorType dosSector = new SectorType ("Bootstrap Loader", Color.lightGray);
  final SectorType catalogSector = new SectorType ("Catalog", new Color (0, 200, 0)); // green
  final SectorType volumeMapSector = new SectorType ("Volume Map", Color.blue);
  final SectorType subcatalogSector = new SectorType ("Subcatalog", Color.magenta);
  final SectorType masterIndexSector = new SectorType ("Master Index", Color.orange);
  final SectorType indexSector = new SectorType ("Index", Color.cyan);
  final SectorType dataSector = new SectorType ("Data", Color.red);
  final SectorType extendedKeySector = new SectorType ("Extended key", Color.gray);

  private final List<DirectoryHeader> headerEntries = new ArrayList<DirectoryHeader> ();
  VolumeDirectoryHeader vdh = null;
  private static final boolean debug = false;

  static final DateFormat df = DateFormat.getInstance ();
  static final SimpleDateFormat sdf = new SimpleDateFormat ("d-MMM-yy");
  static final SimpleDateFormat stf = new SimpleDateFormat ("H:mm");

  public ProdosDisk (Disk disk)
  {
    super (disk);

    sectorTypesList.add (dosSector);
    sectorTypesList.add (catalogSector);
    sectorTypesList.add (subcatalogSector);
    sectorTypesList.add (volumeMapSector);
    sectorTypesList.add (masterIndexSector);
    sectorTypesList.add (indexSector);
    sectorTypesList.add (dataSector);
    sectorTypesList.add (extendedKeySector);

    for (int block = 0; block < 2; block++)
      if (!disk.isSectorEmpty (disk.getDiskAddress (block)))
        sectorTypes[block] = dosSector;

    byte[] buffer = disk.readSector (0);
    bootSector = new BootSector (disk, buffer, "Prodos");

    DefaultMutableTreeNode root = getCatalogTreeRoot ();
    DefaultMutableTreeNode volumeNode = new DefaultMutableTreeNode ("empty volume node");
    root.add (volumeNode);
    processDirectoryBlock (2, null, volumeNode);
    makeNodeVisible (volumeNode.getFirstLeaf ());

    for (DiskAddress da2 : disk)
    {
      int blockNo = da2.getBlock ();
      if (freeBlocks.get (blockNo))
      {
        if (!stillAvailable (da2))
          falsePositives++;
      }
      else if (stillAvailable (da2))
        falseNegatives++;
    }
  }

  private void processDirectoryBlock (int block, FileEntry parent,
        DefaultMutableTreeNode parentNode)
  {
    DirectoryHeader localHeader = null;
    SectorType currentSectorType = null;

    do
    {
      byte[] sectorBuffer = disk.readSector (block);
      sectorTypes[block] = currentSectorType;

      for (int ptr = 4, max = disk.getBlockSize () - ProdosConstants.ENTRY_SIZE; ptr < max; ptr +=
            ProdosConstants.ENTRY_SIZE)
      {
        int storageType = (sectorBuffer[ptr] & 0xF0) >> 4;
        if (storageType == 0) // deleted or unused
          continue;

        byte[] entry = new byte[ProdosConstants.ENTRY_SIZE];
        System.arraycopy (sectorBuffer, ptr, entry, 0, ProdosConstants.ENTRY_SIZE);

        switch (storageType)
        {
          case ProdosConstants.TYPE_DIRECTORY_HEADER:
            assert headerEntries.size () == 0;
            vdh = new VolumeDirectoryHeader (this, entry);
            localHeader = vdh;
            assert localHeader.entryLength == ProdosConstants.ENTRY_SIZE;
            headerEntries.add (localHeader);
            currentSectorType = catalogSector;
            sectorTypes[block] = currentSectorType;
            for (int i = 0; i < vdh.totalBitMapBlocks; i++)
              sectorTypes[vdh.bitMapBlock + i] = volumeMapSector;
            parentNode.setUserObject (vdh); // populate the empty volume node
            break;
          case ProdosConstants.TYPE_SUBDIRECTORY_HEADER:
            localHeader = new SubDirectoryHeader (this, entry, parent);
            headerEntries.add (localHeader);
            currentSectorType = subcatalogSector;
            sectorTypes[block] = currentSectorType;
            break;
          case ProdosConstants.TYPE_SUBDIRECTORY:
            FileEntry ce = new FileEntry (this, entry, localHeader, block);
            fileEntries.add (ce);
            DefaultMutableTreeNode directoryNode = new DefaultMutableTreeNode (ce);
            directoryNode.setAllowsChildren (true);
            parentNode.add (directoryNode);
            processDirectoryBlock (ce.keyPtr, ce, directoryNode); // Recursion !!
            break;
          case ProdosConstants.TYPE_SEEDLING:
          case ProdosConstants.TYPE_SAPLING:
          case ProdosConstants.TYPE_TREE:
          case ProdosConstants.TYPE_PASCAL_ON_PROFILE:
          case ProdosConstants.TYPE_GSOS_EXTENDED_FILE:
            FileEntry fe = new FileEntry (this, entry, localHeader, block);
            fileEntries.add (fe);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode (fe);
            node.setAllowsChildren (false);
            parentNode.add (node);
            break;
          default:
            System.out.println ("Unknown storage type : " + storageType);
            System.out.println (HexFormatter.format (entry, 0, entry.length));
        }
      }
      block = HexFormatter.intValue (sectorBuffer[2], sectorBuffer[3]);
    } while (block > 0);
  }

  public static boolean isCorrectFormat (AppleDisk disk)
  {
    disk.setInterleave (1);
    if (checkFormat (disk))
      return true;
    disk.setInterleave (0);
    return checkFormat (disk);
  }

  public static boolean checkFormat (AppleDisk disk)
  {
    byte[] buffer = disk.readSector (2);          // Prodos KEY BLOCK
    if (debug)
      System.out.println (HexFormatter.format (buffer));

    // check entry length and entries per block
    if (buffer[0x23] != 0x27 || buffer[0x24] != 0x0D)
      return false;
    int bitMapBlock = HexFormatter.intValue (buffer[0x27], buffer[0x28]);
    if (bitMapBlock != 6)
      return false;
    return true;
  }

  public DataSource getFile (int fileNo)
  {
    if (fileNo == 0)
      return ((VolumeDirectoryHeader) headerEntries.get (0)).getDataSource ();
    return fileEntries.get (fileNo - 1).getDataSource ();
  }

  @Override
  public AppleFileSource getCatalog ()
  {
    return new DefaultAppleFileSource ("Catalog", headerEntries.get (0).getDataSource (), this);
  }

  @Override
  public String toString ()
  {
    StringBuffer text = new StringBuffer ();
    String newLine = String.format ("%n");

    VolumeDirectoryHeader volumeDirectory = (VolumeDirectoryHeader) headerEntries.get (0);
    String timeC =
          volumeDirectory.created == null ? "" : df
                .format (volumeDirectory.created.getTime ());
    text.append ("Volume name        : " + volumeDirectory.name + newLine);
    text.append ("Creation date      : " + timeC + newLine);
    text.append ("ProDOS version     : " + volumeDirectory.version + newLine);
    text.append ("Min ProDOS version : " + volumeDirectory.minVersion + newLine);
    text.append ("Access rights      : " + volumeDirectory.access + newLine);
    text.append ("Entry length       : " + volumeDirectory.entryLength + newLine);
    text.append ("Entries per block  : " + volumeDirectory.entriesPerBlock + newLine);
    text.append ("File count         : " + volumeDirectory.fileCount + newLine);
    text.append ("Bitmap block       : " + volumeDirectory.bitMapBlock + newLine);
    text.append ("Total blocks       : " + volumeDirectory.totalBlocks + newLine);

    return text.toString ();
  }

  @Override
  public DataSource getFormattedSector (DiskAddress da)
  {
    if (da.getBlock () == 0)
      return bootSector;

    byte[] buffer = disk.readSector (da);
    SectorType type = sectorTypes[da.getBlock ()];

    if (type == catalogSector || type == subcatalogSector)
      return new ProdosCatalogSector (disk, buffer);
    if (type == volumeMapSector)
      return new ProdosBitMapSector (this, disk, buffer, da);
    if (type == masterIndexSector || type == indexSector)
      return new ProdosIndexSector (getSectorFilename (da), disk, buffer);
    if (type == extendedKeySector)
      return new ProdosExtendedKeySector (disk, buffer);
    if (type == dosSector)
      return new DefaultSector ("Boot sector", disk, buffer);

    String name = getSectorFilename (da);
    if (name != null)
      return new DefaultSector (name, disk, buffer);
    return super.getFormattedSector (da);
  }

  @Override
  public String getSectorFilename (DiskAddress da)
  {
    for (AppleFileSource fe : fileEntries)
      if (((FileEntry) fe).contains (da))
        return ((FileEntry) fe).getUniqueName ();
    return null;
  }

  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  {
    if (fileNo == 0)
      return ((VolumeDirectoryHeader) headerEntries.get (0)).getSectors ();
    return fileEntries.get (fileNo - 1).getSectors ();
  }
}