package com.bytezone.diskbrowser.dos;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.BootSector;
import com.bytezone.diskbrowser.disk.*;
import com.bytezone.diskbrowser.gui.DataSource;

public class DosDisk extends AbstractFormattedDisk
{
  private static final int ENTRY_SIZE = 35;
  private static final int CATALOG_TRACK = 17;

  private final DosVTOCSector dosVTOCSector;
  private final Color green = new Color (0, 200, 0);
  private final DefaultMutableTreeNode volumeNode;

  private int freeSectors;
  private int usedSectors;

  public final SectorType vtocSector = new SectorType ("VTOC", Color.magenta);
  public final SectorType catalogSector = new SectorType ("Catalog", green);
  public final SectorType tsListSector = new SectorType ("TSList", Color.blue);
  public final SectorType dataSector = new SectorType ("Data", Color.red);
  public final SectorType dosSector = new SectorType ("DOS", Color.lightGray);

  protected List<AppleFileSource> deletedFileEntries = new ArrayList<AppleFileSource> ();

  enum FileType
  {
    Text, ApplesoftBasic, IntegerBasic, Binary, Relocatable, SS, AA, BB
  }

  public DosDisk (Disk disk)
  {
    super (disk);

    sectorTypesList.add (dosSector);
    sectorTypesList.add (vtocSector);
    sectorTypesList.add (catalogSector);
    sectorTypesList.add (tsListSector);
    sectorTypesList.add (dataSector);

    byte[] sectorBuffer = disk.readSector (0, 0);               // Boot sector
    bootSector = new BootSector (disk, sectorBuffer, "DOS");

    sectorBuffer = disk.readSector (CATALOG_TRACK, 0);          // VTOC
    dosVTOCSector = new DosVTOCSector (this, disk, sectorBuffer);

    DiskAddress catalogStart = disk.getDiskAddress (sectorBuffer[1], sectorBuffer[2]);

    if (dosVTOCSector.sectorSize != disk.getBlockSize ())
      System.out.println ("Invalid sector size : " + dosVTOCSector.sectorSize);
    if (dosVTOCSector.maxSectors != disk.getSectorsPerTrack ())
      System.out.println ("Invalid sectors per track : " + dosVTOCSector.maxSectors);

    sectorTypes[CATALOG_TRACK * dosVTOCSector.maxSectors] = vtocSector;

    // assert (maxTracks == disk.getTotalTracks ());
    //    assert (dosVTOCSector.maxSectors == disk.getSectorsPerTrack ());
    // assert (sectorSize == disk.getBlockSize ()); HFSAssembler.dsk fails this
    //    assert (catalogStart.getTrack () == CATALOG_TRACK);

    // arcboot.dsk starts the catalog at 17/13
    // assert (catalogStart.getSector () == 15);

    // build list of CatalogEntry objects
    DefaultMutableTreeNode rootNode = getCatalogTreeRoot ();
    volumeNode = new DefaultMutableTreeNode ();
    DefaultMutableTreeNode deletedFilesNode = new DefaultMutableTreeNode ();
    rootNode.add (volumeNode);

    // flag the catalog sectors before any file mistakenly grabs them
    DiskAddress da = disk.getDiskAddress (catalogStart.getBlock ());
    do
    {
      if (!disk.isValidAddress (da))
        break;
      sectorBuffer = disk.readSector (da);
      if (!disk.isValidAddress (sectorBuffer[1], sectorBuffer[2]))
        break;

      // The first byte is officially unused, but it always seems to contain 0x00 or 0xFF
      // See beautifulboot.dsk.
      if (sectorBuffer[0] != 0 && (sectorBuffer[0] & 0xFF) != 0xFF && false)
      {
        System.out
            .println ("Dos catalog sector buffer byte #0 invalid : " + sectorBuffer[0]);
        break;
      }

      sectorTypes[da.getBlock ()] = catalogSector;

      int track = sectorBuffer[1] & 0xFF;
      int sector = sectorBuffer[2] & 0xFF;
      if (!disk.isValidAddress (track, sector))
        break;

      //      int thisBlock = da.getBlock ();
      da = disk.getDiskAddress (track, sector);

      //      if (CHECK_SELF_POINTER && da.getBlock () == thisBlock)
      //        break;

    } while (da.getBlock () != 0);

    // same loop, but now all the catalog sectors are properly flagged
    da = disk.getDiskAddress (catalogStart.getBlock ());
    loop: do
    {
      if (!disk.isValidAddress (da))
        break;
      sectorBuffer = disk.readSector (da);
      if (!disk.isValidAddress (sectorBuffer[1], sectorBuffer[2]))
        break;

      for (int ptr = 11; ptr < 256; ptr += ENTRY_SIZE)
      {
        if (sectorBuffer[ptr] == 0) // empty slot, no more catalog entries
          continue;
        //          break loop;

        byte[] entry = new byte[ENTRY_SIZE];
        System.arraycopy (sectorBuffer, ptr, entry, 0, ENTRY_SIZE);

        if (entry[0] == (byte) 0xFF) // deleted file
        {
          DeletedCatalogEntry deletedCatalogEntry =
              new DeletedCatalogEntry (this, da, entry);
          deletedFileEntries.add (deletedCatalogEntry);
          DefaultMutableTreeNode node = new DefaultMutableTreeNode (deletedCatalogEntry);
          node.setAllowsChildren (false);
          deletedFilesNode.add (node);
        }
        else
        {
          CatalogEntry catalogEntry = new CatalogEntry (this, da, entry);
          fileEntries.add (catalogEntry);
          DefaultMutableTreeNode node = new DefaultMutableTreeNode (catalogEntry);
          node.setAllowsChildren (false);
          volumeNode.add (node);
        }
      }

      int track = sectorBuffer[1] & 0xFF;
      int sector = sectorBuffer[2] & 0xFF;
      if (!disk.isValidAddress (track, sector))
        break;

      //      int thisBlock = da.getBlock ();
      da = disk.getDiskAddress (sectorBuffer[1], sectorBuffer[2]);

    } while (da.getBlock () != 0);

    // add up all the free and used sectors, and label DOS sectors while we're here
    int lastDosSector = dosVTOCSector.maxSectors * 3; // first three tracks
    for (DiskAddress da2 : disk)
    {
      int blockNo = da2.getBlock ();
      if (blockNo < lastDosSector) // in the DOS region
      {
        if (freeBlocks.get (blockNo)) // according to the VTOC
          ++freeSectors;
        else
        {
          ++usedSectors;
          if (sectorTypes[blockNo] == usedSector)
            sectorTypes[blockNo] = dosSector;
        }
      }
      else
      {
        if (stillAvailable (da2)) // free or used, ie not specifically labelled
          ++freeSectors;
        else
          ++usedSectors;
      }

      if (freeBlocks.get (blockNo) && !stillAvailable (da2))
        falsePositives++;
      if (!freeBlocks.get (blockNo) && stillAvailable (da2))
        falseNegatives++;
    }

    if (deletedFilesNode.getDepth () > 0)
    {
      rootNode.add (deletedFilesNode);
      deletedFilesNode.setUserObject (getDeletedList ());
      makeNodeVisible (deletedFilesNode.getFirstLeaf ());
    }
    volumeNode.setUserObject (getCatalog ());
    makeNodeVisible (volumeNode.getFirstLeaf ());
  }

  @Override
  public void setOriginalPath (Path path)
  {
    super.setOriginalPath (path);
    volumeNode.setUserObject (getCatalog ());  // this has already been set in the constructor
  }

  // Beagle Bros FRAMEUP disk only has one catalog block
  // ARCBOOT.DSK has a catalog which starts at sector 0C
  public static boolean isCorrectFormat (AppleDisk disk)
  {
    disk.setInterleave (0);
    int catalogBlocks = checkFormat (disk);
    if (catalogBlocks > 3)
      return true;
    disk.setInterleave (1);
    int cb2 = checkFormat (disk);
    //    if (cb2 > catalogBlocks)
    if (cb2 > 3)
      return true;
    disk.setInterleave (2);
    if (false)
    {
      int cb3 = checkFormat (disk);
      if (cb3 > 3)
        return true;
    }
    if (catalogBlocks > 0)
    {
      disk.setInterleave (0);
      return true;
    }
    if (cb2 > 0)
      return true;
    return false;
  }

  private static int checkFormat (AppleDisk disk)
  {
    byte[] buffer = disk.readSector (0x11, 0x00);

    // DISCCOMMANDER.DSK uses track 0x17 for the catalog
    //    if (buffer[1] != 0x11) // first catalog track
    //      return 0;

    if (buffer[53] != 16 && buffer[53] != 13)         // tracks per sector
      return 0;

    if (buffer[49] < -1 || buffer[49] > 1)            // direction of next file save
    {
      System.out.println ("Bad direction : " + buffer[49]);
      // Visicalc data disk had 0xF8
      //      return 0;
    }

    int version = buffer[3];
    if (version < -1 || version > 4)
    {
      System.out.println ("Bad version : " + buffer[3]);
      return 0;
    }

    return countCatalogBlocks (disk, buffer);
  }

  private static int countCatalogBlocks (AppleDisk disk, byte[] buffer)
  {
    DiskAddress catalogStart = disk.getDiskAddress (buffer[1], buffer[2]);
    //    int catalogBlocks = 0;
    DiskAddress da = disk.getDiskAddress (catalogStart.getBlock ());
    List<DiskAddress> catalogAddresses = new ArrayList<DiskAddress> ();

    do
    {
      if (!disk.isValidAddress (da))
        break;

      if (catalogAddresses.contains (da))
      {
        System.out.println ("Catalog looping");
        return 0;
      }

      buffer = disk.readSector (da);
      if (!disk.isValidAddress (buffer[1], buffer[2]))
      {
        System.out.printf ("Invalid address : %02X / %02X%n", buffer[1], buffer[2]);
        break;
      }

      catalogAddresses.add (da);
      //      catalogBlocks++;
      //      if (catalogBlocks > 1000)     // looping
      //      {
      //        System.out.println ("Disk appears to be looping in countCatalogBlocks()");
      //        return 0;
      //      }

      //      int thisBlock = da.getBlock ();
      da = disk.getDiskAddress (buffer[1], buffer[2]);

    } while (da.getBlock () != 0);

    //    if (catalogBlocks != catalogAddresses.size ())
    //      System.out.printf ("CB: %d, size: %d%n", catalogBlocks, catalogAddresses.size ());
    return catalogAddresses.size ();
  }

  @Override
  public String toString ()
  {
    StringBuffer text = new StringBuffer (dosVTOCSector.toString ());
    return text.toString ();
  }

  @Override
  public DataSource getFormattedSector (DiskAddress da)
  {
    SectorType type = sectorTypes[da.getBlock ()];
    if (type == vtocSector)
      return dosVTOCSector;
    if (da.getBlock () == 0)
      return bootSector;

    byte[] buffer = disk.readSector (da);
    String address = String.format ("%02X %02X", da.getTrack (), da.getSector ());

    if (type == tsListSector)
      return new DosTSListSector (getSectorFilename (da), disk, buffer);
    if (type == catalogSector)
      return new DosCatalogSector (disk, buffer);
    if (type == dataSector)
      return new DefaultSector (
          "Data Sector at " + address + " : " + getSectorFilename (da), disk, buffer);
    if (type == dosSector)
      return new DefaultSector ("DOS sector at " + address, disk, buffer);
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
    if (fileEntries.size () > 0 && fileEntries.size () > fileNo)
      return fileEntries.get (fileNo).getSectors ();
    return null;
  }

  @Override
  public AppleFileSource getCatalog ()
  {
    String newLine = String.format ("%n");
    String line = "- --- ---  ------------------------------  -----  -------------"
        + "  -- ----  ----------------" + newLine;
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Disk : %s%n%n", getAbsolutePath ()));
    text.append ("L Typ Len  Name                            Addr"
        + "   Length         TS Data  Comment" + newLine);
    text.append (line);

    for (AppleFileSource ce : fileEntries)
      text.append (((CatalogEntry) ce).getDetails () + newLine);

    text.append (line);
    text.append (String.format (
                                "           Free sectors: %3d    Used sectors: %3d    Total sectors: %3d",
                                dosVTOCSector.freeSectors, dosVTOCSector.usedSectors,
                                (dosVTOCSector.freeSectors + dosVTOCSector.usedSectors)));
    if (dosVTOCSector.freeSectors != freeSectors)
      text.append (String.format (
                                  "%nActual:    Free sectors: %3d    Used sectors: %3d    Total sectors: %3d",
                                  freeSectors, usedSectors, (usedSectors + freeSectors)));
    return new DefaultAppleFileSource ("Volume " + dosVTOCSector.volume, text.toString (),
        this);
  }

  private AppleFileSource getDeletedList ()
  {
    StringBuilder text =
        new StringBuilder ("List of files that were deleted from this disk\n");

    for (AppleFileSource afs : deletedFileEntries)
      text.append (((DeletedCatalogEntry) afs).getDetails () + "\n");

    return new DefaultAppleFileSource ("Deleted files", text.toString (), this);
  }
}