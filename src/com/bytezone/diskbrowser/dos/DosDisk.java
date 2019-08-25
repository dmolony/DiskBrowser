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
  private static final int VTOC_SECTOR = 0;

  final DosVTOCSector dosVTOCSector;
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

    DiskAddress da = disk.getDiskAddress (0, 0);
    byte[] sectorBuffer = disk.readSector (da);               // Boot sector
    bootSector = new BootSector (disk, sectorBuffer, "DOS", da);

    da = disk.getDiskAddress (CATALOG_TRACK, VTOC_SECTOR);
    sectorBuffer = disk.readSector (da);          // VTOC
    dosVTOCSector = new DosVTOCSector (this, disk, sectorBuffer, da);
    sectorTypes[da.getBlock ()] = vtocSector;

    DiskAddress catalogStart = disk.getDiskAddress (sectorBuffer[1], sectorBuffer[2]);

    if (dosVTOCSector.sectorSize != disk.getBlockSize ())
      System.out.println ("Invalid sector size : " + dosVTOCSector.sectorSize);
    if (dosVTOCSector.maxSectors != disk.getSectorsPerTrack ())
      System.out.println ("Invalid sectors per track : " + dosVTOCSector.maxSectors);

    //    sectorTypes[CATALOG_TRACK * dosVTOCSector.maxSectors] = vtocSector;

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
    da = disk.getDiskAddress (catalogStart.getBlock ());
    do
    {
      if (!disk.isValidAddress (da))
        break;
      sectorBuffer = disk.readSector (da);
      if (!disk.isValidAddress (sectorBuffer[1], sectorBuffer[2]))
        break;

      // The first byte is officially unused, but it always seems to contain 0x00 or 0xFF
      // See beautifulboot.dsk.
      //      if (sectorBuffer[0] != 0 && (sectorBuffer[0] & 0xFF) != 0xFF && false)
      //      {
      //        System.out
      //            .println ("Dos catalog sector buffer byte #0 invalid : " + sectorBuffer[0]);
      //        break;
      //      }

      sectorTypes[da.getBlock ()] = catalogSector;

      int track = sectorBuffer[1] & 0xFF;
      int sector = sectorBuffer[2] & 0xFF;
      if (!disk.isValidAddress (track, sector))
        break;

      da = disk.getDiskAddress (track, sector);

    } while (da.getBlock () != 0);

    // same loop, but now all the catalog sectors are properly flagged
    da = disk.getDiskAddress (catalogStart.getBlock ());
    do
    {
      if (!disk.isValidAddress (da))
        break;
      sectorBuffer = disk.readSector (da);
      if (!disk.isValidAddress (sectorBuffer[1], sectorBuffer[2]))
        break;

      for (int ptr = 11; ptr < 256; ptr += ENTRY_SIZE)
      {
        if (sectorBuffer[ptr] == 0)         // empty slot, no more catalog entries
          continue;

        byte[] entry = new byte[ENTRY_SIZE];
        System.arraycopy (sectorBuffer, ptr, entry, 0, ENTRY_SIZE);
        int track = entry[0] & 0xFF;
        boolean deletedFlag = (entry[0] & 0x80) != 0;

        if (deletedFlag)              // deleted file
        {
          DeletedCatalogEntry deletedCatalogEntry =
              new DeletedCatalogEntry (this, da, entry, dosVTOCSector.dosVersion);
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
      if (dosVTOCSector.dosVersion >= 0x41)
      {
        track = track & 0x3F;
        sector = sector & 0x1F;
      }

      if (!disk.isValidAddress (track, sector))
        break;

      da = disk.getDiskAddress (sectorBuffer[1], sectorBuffer[2]);

    } while (da.getBlock () != 0);

    // link double hi-res files
    for (AppleFileSource fe : fileEntries)
    {
      String name = fe.getUniqueName ();
      if (name.endsWith (".AUX"))
      {
        String partner1 = name.substring (0, name.length () - 4);
        String partner2 = partner1 + ".BIN";
        for (AppleFileSource fe2 : fileEntries)
          if (fe2.getUniqueName ().equals (partner1)
              || fe2.getUniqueName ().equals (partner2))
          {
            ((CatalogEntry) fe2).link ((CatalogEntry) fe);
            ((CatalogEntry) fe).link ((CatalogEntry) fe2);
          }
      }
    }

    // add up all the free and used sectors, and label DOS sectors while we're here
    int lastDosSector = dosVTOCSector.maxSectors * 3;       // first three tracks
    for (DiskAddress da2 : disk)
    {
      int blockNo = da2.getBlock ();
      if (blockNo < lastDosSector) // in the DOS region
      {
        if (freeBlocks.get (blockNo))                       // according to the VTOC
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
        if (stillAvailable (da2))       // free or used, ie not specifically labelled
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
    if (true)
    {
      int cb3 = checkFormat (disk);
      if (cb3 > 3)
        return true;
    }

    if (catalogBlocks > 0)
    {
      disk.setInterleave (1);
      return true;
    }

    if (cb2 > 0)
      return true;

    return false;
  }

  public String getVersionText ()
  {
    switch (getVersion ())
    {
      case 0x01:
        return "3.1";
      case 0x02:
        return "3.2";
      case 0x03:
        return "3.3";
      case 0x41:
        return "4.1";
      case 0x42:
        return "4.2";
      case 0x43:
        return "4.3";
      default:
        return "??";
    }
  }

  public int getVersion ()
  {
    return dosVTOCSector.dosVersion;
  }

  private static int checkFormat (AppleDisk disk)
  {
    byte[] buffer = disk.readSector (0x11, 0x00);

    // DISCCOMMANDER.DSK uses track 0x17 for the catalog
    //    if (buffer[1] != 0x11) // first catalog track
    //      return 0;

    if (buffer[53] != 16 && buffer[53] != 13)         // sectors per track
    {
      return 0;
    }

    //    if (buffer[49] < -1 || buffer[49] > 1)      // direction of next file save
    //    {
    //      System.out.println ("Bad direction : " + buffer[49]);
    //      // Visicalc data disk had 0xF8
    //      //      return 0;
    //    }

    int version = buffer[3] & 0xFF;
    if (version > 0x43 && version != 0xFF)
    {
      System.out.printf ("Bad version : %02X%n", version);
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
        return 0;

      if (catalogAddresses.contains (da))
      {
        System.out.println ("Catalog looping");
        return 0;
      }

      buffer = disk.readSector (da);
      if (!disk.isValidAddress (buffer[1], buffer[2]))
      {
        //        System.out.printf ("Invalid address : %02X / %02X%n", buffer[1], buffer[2]);
        return 0;
      }

      catalogAddresses.add (da);

      da = disk.getDiskAddress (buffer[1], buffer[2]);

    } while (da.getBlock () != 0);

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
      return new DosTSListSector (getSectorFilename (da), disk, buffer, da);
    if (type == catalogSector)
      return new DosCatalogSector (this, disk, buffer, da);
    if (type == dataSector)
      return new DefaultSector (
          "Data Sector at " + address + " : " + getSectorFilename (da), disk, buffer, da);
    if (type == dosSector)
      return new DefaultSector ("DOS sector at " + address, disk, buffer, da);
    return super.getFormattedSector (da);
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
        + "  -- ----  -------------------" + newLine;
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Disk : %s%n%n", getDisplayPath ()));
    text.append ("L Typ Len  Name                            Addr"
        + "   Length         TS Data  Comment" + newLine);
    text.append (line);

    for (AppleFileSource fileEntry : fileEntries)
      text.append (((CatalogEntry) fileEntry).getDetails () + newLine);

    text.append (line);
    text.append (String.format (
        "           Free sectors: %3d    " + "Used sectors: %3d    Total sectors: %3d",
        dosVTOCSector.freeSectors, dosVTOCSector.usedSectors,
        (dosVTOCSector.freeSectors + dosVTOCSector.usedSectors)));
    if (dosVTOCSector.freeSectors != freeSectors)
      text.append (String.format (
          "%nActual:    Free sectors: %3d    "
              + "Used sectors: %3d    Total sectors: %3d",
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

  /* From http://apple2history.org/history/ah15/
   *
    There were actually three versions of DOS 3.3 that Apple released without
    bumping the version number:
  
    The first version that was released had FPBASIC and INTBASIC files that were 50
    sectors in size.
  
    The second version of DOS 3.3, often referred to as “DOS 3.3e”, appeared at the
    time the Apple IIe was released. In this version, the FPBASIC and INTBASIC files
    were 42 sectors in size. The changes introduced at that time included code to turn
    off the IIe 80-column card at boot time, and an attempt to fix a bug in the APPEND
    command. This fix reportedly introduced an even worse bug, but as the command was
    not heavily used it did not make much of an impact on most programmers. The APPEND
    fix was applied by utilizing some formerly unused space in the DOS 3.3 code.
  
    The third version of DOS 3.3 appeared just before the first release of ProDOS.
    The only mention of this in the press was in the DOSTalk column of Softalk magazine.
    This final version of DOS 3.3 included a different fix for the APPEND bug, using
    another bit of unused space in DOS 3.3.
  
    With regard to the FPBASIC and INTBASIC files: There were three differences between
    the 50 sector and the 42 sector versions of the INTBASIC file. Firstly, the
    $F800-$FFFF section was removed. This area was the code for the Monitor, and with
    the changes introduced in the Apple IIe, it could cause some things to “break” if
    the older Monitor code was executed. Secondly, a FOR/NEXT bug in Integer BASIC was
    fixed. Finally, there was a three-byte bug in the Programmer’s Aid ROM #1 chip.
    The code for this chip was included in the INTBASIC file, and could therefore be
    patched.
   */
}