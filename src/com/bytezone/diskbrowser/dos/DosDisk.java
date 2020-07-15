package com.bytezone.diskbrowser.dos;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
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

// -----------------------------------------------------------------------------------//
public class DosDisk extends AbstractFormattedDisk
// -----------------------------------------------------------------------------------//
{
  private static final int ENTRY_SIZE = 35;
  private static final int CATALOG_TRACK = 17;
  private static final int VTOC_SECTOR = 0;

  final DosVTOCSector dosVTOCSector;
  private final Color green = new Color (0, 200, 0);
  private final DefaultMutableTreeNode volumeNode;

  private int freeSectors;
  private int usedSectors;
  private final int volumeNo;             // for multi-volume disks

  public final SectorType vtocSector = new SectorType ("VTOC", Color.magenta);
  public final SectorType catalogSector = new SectorType ("Catalog", green);
  public final SectorType tsListSector = new SectorType ("TSList", Color.blue);
  public final SectorType dataSector = new SectorType ("Data", Color.red);
  public final SectorType dosSector = new SectorType ("DOS", Color.lightGray);

  protected List<AppleFileSource> deletedFileEntries = new ArrayList<> ();

  private static boolean debug = false;

  enum FileType
  {
    Text, ApplesoftBasic, IntegerBasic, Binary, Relocatable, SS, AA, BB
  }

  // ---------------------------------------------------------------------------------//
  public DosDisk (Disk disk)
  // ---------------------------------------------------------------------------------//
  {
    this (disk, 0);
  }

  // ---------------------------------------------------------------------------------//
  public DosDisk (Disk disk, int volumeNo)
  // ---------------------------------------------------------------------------------//
  {
    super (disk);

    this.volumeNo = volumeNo;

    sectorTypesList.add (dosSector);
    sectorTypesList.add (vtocSector);
    sectorTypesList.add (catalogSector);
    sectorTypesList.add (tsListSector);
    sectorTypesList.add (dataSector);

    DiskAddress da = disk.getDiskAddress (0, 0);
    byte[] sectorBuffer = disk.readBlock (da);               // Boot sector
    bootSector = new BootSector (disk, sectorBuffer, "DOS", da);

    da = disk.getDiskAddress (CATALOG_TRACK, VTOC_SECTOR);
    sectorBuffer = disk.readBlock (da);          // VTOC
    dosVTOCSector = new DosVTOCSector (this, disk, sectorBuffer, da);
    sectorTypes[da.getBlockNo ()] = vtocSector;

    DiskAddress catalogStart = disk.getDiskAddress (sectorBuffer[1], sectorBuffer[2]);

    if (dosVTOCSector.sectorSize != disk.getBlockSize ())
      System.out.printf ("%s - invalid sector size : %d%n", disk.getFile ().getName (),
          dosVTOCSector.sectorSize);
    if (dosVTOCSector.maxSectors != disk.getBlocksPerTrack ())
      System.out.printf ("%s - invalid sectors per track : %d%n",
          disk.getFile ().getName (), dosVTOCSector.maxSectors);

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
    da = disk.getDiskAddress (catalogStart.getBlockNo ());
    do
    {
      if (!disk.isValidAddress (da))
        break;
      sectorBuffer = disk.readBlock (da);
      if (!disk.isValidAddress (sectorBuffer[1], sectorBuffer[2]))
        break;

      // The first byte is officially unused, but it always seems to contain 0x00 or 0xFF
      // See beautifulboot.dsk.
      //      if (sectorBuffer[0] != 0 && (sectorBuffer[0] & 0xFF) != 0xFF && false)
      //      {
      //        System.out
      //       .println ("Dos catalog sector buffer byte #0 invalid : " + sectorBuffer[0]);
      //        break;
      //      }

      sectorTypes[da.getBlockNo ()] = catalogSector;

      int track = sectorBuffer[1] & 0xFF;
      int sector = sectorBuffer[2] & 0xFF;
      if (!disk.isValidAddress (track, sector))
        break;

      da = disk.getDiskAddress (track, sector);

    } while (!da.isZero ());

    // same loop, but now all the catalog sectors are properly flagged
    da = disk.getDiskAddress (catalogStart.getBlockNo ());
    loop: do
    {
      if (!disk.isValidAddress (da))
        break;
      sectorBuffer = disk.readBlock (da);
      if (!disk.isValidAddress (sectorBuffer[1], sectorBuffer[2]))
        break;

      for (int ptr = 11; ptr < 256; ptr += ENTRY_SIZE)
      {
        if (sectorBuffer[ptr] == 0)         // empty slot, no more catalog entries
          break loop;

        byte[] entryBuffer = new byte[ENTRY_SIZE];
        System.arraycopy (sectorBuffer, ptr, entryBuffer, 0, ENTRY_SIZE);
        int track = entryBuffer[0] & 0xFF;
        boolean deletedFlag = (entryBuffer[0] & 0x80) != 0;

        if (deletedFlag)              // deleted file
        {
          DeletedCatalogEntry deletedCatalogEntry =
              new DeletedCatalogEntry (this, da, entryBuffer, dosVTOCSector.dosVersion);
          deletedFileEntries.add (deletedCatalogEntry);
          DefaultMutableTreeNode node = new DefaultMutableTreeNode (deletedCatalogEntry);
          node.setAllowsChildren (false);
          deletedFilesNode.add (node);
        }
        else
        {
          CatalogEntry catalogEntry = new CatalogEntry (this, da, entryBuffer);
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

    } while (!da.isZero ());

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
      int blockNo = da2.getBlockNo ();
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

  // ---------------------------------------------------------------------------------//
  //  private int getVolumeNo ()
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    return volumeNo;
  //  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setOriginalPath (Path path)
  // ---------------------------------------------------------------------------------//
  {
    super.setOriginalPath (path);

    // this has already been set in the constructor
    volumeNode.setUserObject (getCatalog ());
  }

  // Beagle Bros FRAMEUP disk only has one catalog block
  // ARCBOOT.DSK has a catalog which starts at sector 0C
  // ---------------------------------------------------------------------------------//
  public static boolean isCorrectFormat (AppleDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    if (false)            // testing
    {
      disk.setInterleave (1);
      return true;
    }

    int blocksPerTrack = disk.getBlocksPerTrack ();
    if (blocksPerTrack > 16 && blocksPerTrack != 32)      // 32 = unidos
    {
      if (debug)
        System.out.printf ("Blocks per track: %d", blocksPerTrack);
      return false;
    }

    int[] cb = new int[3];
    int best = 0;
    int il = -1;
    int max = disk.getBlocksPerTrack () == 16 ? 3 : 1;    // no interleave for 13 sector?

    for (int interleave = 0; interleave < max; interleave++)
    {
      if (debug)
        System.out.printf ("Checking interleave %d%n", interleave);

      disk.setInterleave (interleave);
      cb[interleave] = checkFormat (disk);
      if (cb[interleave] >= 15)
        return true;

      if (cb[interleave] > best)
      {
        best = cb[interleave];
        il = interleave;
      }
    }

    if (best <= 1)
      return false;

    disk.setInterleave (il);
    return true;
  }

  // ---------------------------------------------------------------------------------//
  public String getVersionText ()
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  public int getVersion ()
  // ---------------------------------------------------------------------------------//
  {
    return dosVTOCSector.dosVersion;
  }

  // ---------------------------------------------------------------------------------//
  private static int checkFormat (AppleDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = disk.readBlock (0x11, 0x00);

    // DISCCOMMANDER.DSK uses track 0x17 for the catalog
    //    if (buffer[1] != 0x11) // first catalog track
    //      return 0;

    // Apple Assembly Language.dsk claims 0x2A tracks per disk
    //    if (buffer[52] != 35 && buffer[52] != 50)
    //    {
    //      if (debug)
    //        System.out.printf ("Bad tracks per disk : %02X%n", buffer[52]);
    //      return 0;
    //    }

    if (debug)
      System.out.printf ("Sectors per track: %02X%n", buffer[53]);

    if (buffer[53] != 16 && buffer[53] != 13 && buffer[53] != 32)  // sectors per track
    {
      if (debug)
        System.out.printf ("Bad sectors per track : %02X%n", buffer[53]);
      return 0;
    }

    //    if (buffer[49] < -1 || buffer[49] > 1)      // direction of next file save
    //    {
    //      System.out.println ("Bad direction : " + buffer[49]);
    //      // Visicalc data disk had 0xF8
    //      //      return 0;
    //    }

    int version = buffer[3] & 0xFF;
    if (debug)
      System.out.printf ("Version: %02X%n", buffer[3]);
    if (version == 0 || (version > 0x43 && version != 0xFF))
    {
      if (debug)
        System.out.printf ("Bad version : %02X%n", version);
      return 0;
    }

    int catalogBlocks = countCatalogBlocks (disk, buffer);
    if (debug)
      System.out.printf ("Catalog blocks: %s%n", catalogBlocks);

    return catalogBlocks;
  }

  // ---------------------------------------------------------------------------------//
  private static int countCatalogBlocks (AppleDisk disk, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    DiskAddress catalogStart = disk.getDiskAddress (buffer[1], buffer[2]);
    DiskAddress da = disk.getDiskAddress (catalogStart.getBlockNo ());
    List<DiskAddress> catalogAddresses = new ArrayList<> ();

    do
    {
      if (debug)
        System.out.printf ("Checking: %s%n", da);

      if (!disk.isValidAddress (da))
      {
        if (debug)
          System.out.printf ("Invalid address: %s%n", da);
        return 0;
      }

      if (isDuplicate (catalogAddresses, da))
      {
        if (debug)
          System.out.println ("Catalog looping");
        return 0;
      }

      buffer = disk.readBlock (da);
      if (!disk.isValidAddress (buffer[1], buffer[2]))
      {
        if (debug)
          System.out.printf ("Invalid address: %02X %02X%n", buffer[1], buffer[2]);
        return catalogAddresses.size ();
      }

      catalogAddresses.add (da);

      da = disk.getDiskAddress (buffer[1], buffer[2]);

    } while (!da.isZero ());

    if (debug)
      System.out.printf ("Catalog blocks: %d%n", catalogAddresses.size ());
    return catalogAddresses.size ();
  }

  // ---------------------------------------------------------------------------------//
  private static boolean isDuplicate (List<DiskAddress> catalogAddresses, DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    for (DiskAddress diskAddress : catalogAddresses)
      if (diskAddress.getBlockNo () == da.getBlockNo ())
        return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Disk name ............. %s%n", getDisplayPath ()));
    text.append (
        String.format ("DOS version ........... %s%n", dosVTOCSector.dosVersion));
    text.append (
        String.format ("Sectors per track ..... %d%n", dosVTOCSector.maxSectors));
    text.append (String.format ("Volume no ............. %d%n", volumeNo));
    text.append (String.format ("Interleave ............ %d", disk.getInterleave ()));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getFormattedSector (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    SectorType type = sectorTypes[da.getBlockNo ()];
    if (type == vtocSector)
      return dosVTOCSector;
    if (da.isZero ())
      return bootSector;

    byte[] buffer = disk.readBlock (da);
    String address = String.format ("%02X %02X", da.getTrackNo (), da.getSectorNo ());

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
  @Override
  public AppleFileSource getCatalog ()
  // ---------------------------------------------------------------------------------//
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

    String volumeText = volumeNo == 0 ? "" : "Side " + volumeNo + " ";

    return new DefaultAppleFileSource (volumeText + "DOS Volume " + dosVTOCSector.volume,
        text.toString (), this);
  }

  // ---------------------------------------------------------------------------------//
  private AppleFileSource getDeletedList ()
  // ---------------------------------------------------------------------------------//
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