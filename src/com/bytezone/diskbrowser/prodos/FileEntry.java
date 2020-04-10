package com.bytezone.diskbrowser.prodos;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.bytezone.diskbrowser.applefile.ApplesoftBasicProgram;
import com.bytezone.diskbrowser.applefile.AssemblerProgram;
import com.bytezone.diskbrowser.applefile.BasicProgramGS;
import com.bytezone.diskbrowser.applefile.CharacterRom;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.applefile.DeviceDriver;
import com.bytezone.diskbrowser.applefile.DoubleHiResImage;
import com.bytezone.diskbrowser.applefile.ErrorMessageFile;
import com.bytezone.diskbrowser.applefile.FaddenHiResImage;
import com.bytezone.diskbrowser.applefile.FileSystemTranslator;
import com.bytezone.diskbrowser.applefile.FileTypeDescriptorTable;
import com.bytezone.diskbrowser.applefile.FinderData;
import com.bytezone.diskbrowser.applefile.FontFile;
import com.bytezone.diskbrowser.applefile.HiResImage;
import com.bytezone.diskbrowser.applefile.IconFile;
import com.bytezone.diskbrowser.applefile.IntegerBasicProgram;
import com.bytezone.diskbrowser.applefile.LodeRunner;
import com.bytezone.diskbrowser.applefile.MerlinSource;
import com.bytezone.diskbrowser.applefile.OriginalHiResImage;
import com.bytezone.diskbrowser.applefile.QuickDrawFont;
import com.bytezone.diskbrowser.applefile.SHRPictureFile1;
import com.bytezone.diskbrowser.applefile.SHRPictureFile2;
import com.bytezone.diskbrowser.applefile.ShapeTable;
import com.bytezone.diskbrowser.applefile.SimpleText;
import com.bytezone.diskbrowser.applefile.StoredVariables;
import com.bytezone.diskbrowser.applefile.TextBuffer;
import com.bytezone.diskbrowser.applefile.TextFile;
import com.bytezone.diskbrowser.appleworks.AppleworksADBFile;
import com.bytezone.diskbrowser.appleworks.AppleworksSSFile;
import com.bytezone.diskbrowser.appleworks.AppleworksWPFile;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// - Set sector types for each used sector
// - Populate dataBlocks, indexBlocks, catalogBlock and masterIndexBlock
// - Provide getDataSource ()

// -----------------------------------------------------------------------------------//
class FileEntry extends CatalogEntry implements ProdosConstants
// -----------------------------------------------------------------------------------//
{
  private final int fileType;
  final int keyPtr;
  private final int blocksUsed;
  private final int endOfFile;
  private final int auxType;
  private final GregorianCalendar modified;
  private final int headerPointer;
  private DataSource file;
  private final DiskAddress catalogBlock;

  private DiskAddress masterIndexBlock;
  private final List<DiskAddress> indexBlocks = new ArrayList<> ();

  private boolean invalid;
  private FileEntry link;

  // ---------------------------------------------------------------------------------//
  FileEntry (ProdosDisk fDisk, byte[] entryBuffer, DirectoryHeader parent,
      int parentBlock)
  // ---------------------------------------------------------------------------------//
  {
    super (fDisk, entryBuffer);

    assert parent != null;
    this.parentDirectory = parent;
    this.catalogBlock = this.disk.getDiskAddress (parentBlock);

    fileType = entryBuffer[0x10] & 0xFF;
    keyPtr = HexFormatter.unsignedShort (entryBuffer, 0x11);
    blocksUsed = HexFormatter.unsignedShort (entryBuffer, 0x13);
    endOfFile = HexFormatter.intValue (entryBuffer[21], entryBuffer[22], entryBuffer[23]);

    auxType = HexFormatter.unsignedShort (entryBuffer, 0x1F);
    modified = HexFormatter.getAppleDate (entryBuffer, 0x21);
    headerPointer = HexFormatter.unsignedShort (entryBuffer, 0x25);

    switch (storageType)
    {
      case SEEDLING:
      case SAPLING:
      case TREE:
        addDataBlocks (storageType, keyPtr);
        break;

      case GSOS_EXTENDED_FILE:
        readForks ();
        break;

      case SUBDIRECTORY:
        int block = keyPtr;
        do
        {
          DiskAddress diskAddress = disk.getDiskAddress (block);
          if (diskAddress == null)
            break;
          dataBlocks.add (diskAddress);
          byte[] buffer = disk.readBlock (block);
          block = HexFormatter.unsignedShort (buffer, 2);
        } while (block > 0);
        break;

      case PASCAL_ON_PROFILE:
        indexBlocks.add (disk.getDiskAddress (keyPtr));
        System.out.println ("PASCAL on PROFILE: " + name);    // PDUCSD12.PO
        // are these blocks guaranteed to be contiguous?
        break;

      default:
        System.out.println ("Unknown storage type: " + storageType);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void readForks ()
  // ---------------------------------------------------------------------------------//
  {
    parentDisk.setSectorType (keyPtr, parentDisk.extendedKeySector);
    indexBlocks.add (disk.getDiskAddress (keyPtr));

    byte[] buffer2 = disk.readBlock (keyPtr);        // data fork and resource fork

    // read 2 mini entries (data fork & resource fork)
    for (int i = 0; i < 512; i += 256)
    {
      int storageType = buffer2[i] & 0x0F;
      int keyBlock = HexFormatter.unsignedShort (buffer2, i + 1);
      int eof = HexFormatter.intValue (buffer2[i + 3], buffer2[i + 4], buffer2[i + 5]);
      addDataBlocks (storageType, keyBlock);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void addDataBlocks (int storageType, int keyPtr)
  // ---------------------------------------------------------------------------------//
  {
    DiskAddress emptyDiskAddress = disk.getDiskAddress (0);
    List<Integer> blocks = new ArrayList<> ();

    switch (storageType)
    {
      case SEEDLING:
        if (isValid (keyPtr))
          blocks.add (keyPtr);
        break;

      case SAPLING:
        if (isValid (keyPtr))
          blocks.addAll (readIndex (keyPtr));
        break;

      case TREE:
        if (isValid (keyPtr))
          for (Integer indexBlock : readMasterIndex (keyPtr))
            if (isValid (indexBlock))
              blocks.addAll (readIndex (indexBlock));
        break;
    }

    // remove trailing empty blocks
    while (blocks.size () > 0 && blocks.get (blocks.size () - 1) == 0)
      blocks.remove (blocks.size () - 1);

    for (Integer block : blocks)
    {
      if (block == 0)
        dataBlocks.add (emptyDiskAddress);
      else
      {
        parentDisk.setSectorType (block, parentDisk.dataSector);
        dataBlocks.add (disk.getDiskAddress (block));
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private List<Integer> readIndex (int blockPtr)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> blocks = new ArrayList<> (256);

    if (blockPtr == 0)                    // master index contains a zero
      for (int i = 0; i < 256; i++)
        blocks.add (0);
    else
    {
      parentDisk.setSectorType (blockPtr, parentDisk.indexSector);
      indexBlocks.add (disk.getDiskAddress (blockPtr));

      byte[] buffer = disk.readBlock (blockPtr);
      for (int i = 0; i < 256; i++)
      {
        int blockNo = (buffer[i] & 0xFF) | ((buffer[i + 0x100] & 0xFF) << 8);
        blocks.add (isValid (blockNo) ? blockNo : 0);
      }
    }

    return blocks;
  }

  // ---------------------------------------------------------------------------------//
  private List<Integer> readMasterIndex (int keyPtr)
  // ---------------------------------------------------------------------------------//
  {
    masterIndexBlock = disk.getDiskAddress (keyPtr);
    parentDisk.setSectorType (keyPtr, parentDisk.masterIndexSector);
    indexBlocks.add (disk.getDiskAddress (keyPtr));

    byte[] buffer = disk.readBlock (keyPtr);                   // master index

    int highest = 0x80;
    while (highest-- > 0)                                       // decrement after test
      if (buffer[highest] != 0 || buffer[highest + 0x100] != 0)
        break;

    List<Integer> blocks = new ArrayList<> (highest + 1);
    for (int i = 0; i <= highest; i++)
    {
      int blockNo = (buffer[i] & 0xFF) | ((buffer[i + 256] & 0xFF) << 8);
      blocks.add (isValid (blockNo) ? blockNo : 0);
    }

    return blocks;
  }

  // ---------------------------------------------------------------------------------//
  private boolean isValid (int blockNo)
  // ---------------------------------------------------------------------------------//
  {
    if (false)
    {
      if (!disk.isValidAddress (blockNo))
        System.out.println ("--Invalid Block Address: " + blockNo);
      if (parentDisk.isSectorFree (blockNo))
        System.out.println ("--Free block: " + blockNo);
    }
    return disk.isValidAddress (blockNo) && !parentDisk.isSectorFree (blockNo);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getDataSource ()
  // ---------------------------------------------------------------------------------//
  {
    if (file != null)
      return file;

    if (invalid)
    {
      file = new DefaultAppleFile (name, null);
      return file;
    }

    if (fileType == FILE_TYPE_TEXT && auxType > 0)      // random access file
      return getRandomAccessTextFile ();

    byte[] buffer = getBuffer ();
    byte[] exactBuffer = getExactBuffer (buffer);

    try
    {
      switch (fileType)
      {
        case FILE_TYPE_USER_DEFINED_1:                  // OVL
          if (endOfFile == 0x2000 && auxType == 0)
          {
            file = new OriginalHiResImage (name, exactBuffer, auxType);
            break;
          }
          // drop through
        case FILE_TYPE_BINARY:
        case FILE_TYPE_RELOCATABLE:
        case FILE_TYPE_SYS:
        case FILE_TYPE_BAT:
          if (SimpleText.isHTML (exactBuffer))
            file = new SimpleText (name, exactBuffer);
          else if (HiResImage.isGif (exactBuffer) || HiResImage.isPng (exactBuffer))
            file = new OriginalHiResImage (name, exactBuffer, auxType);
          else if (name.endsWith (".BMP") && HiResImage.isBmp (exactBuffer))
            file = new OriginalHiResImage (name, exactBuffer, auxType);
          else if (name.endsWith (".3200"))           // $C1/02
            file = new SHRPictureFile2 (name, exactBuffer, 0xC1, 0x02, endOfFile);
          else if (name.endsWith (".3201") || HiResImage.isAPP (exactBuffer)) // $C0/04
            // I made up aux=99 to test it without stepping on aux==04
            file = new SHRPictureFile2 (name, exactBuffer, 0xC0, 99, endOfFile);
          else if (name.endsWith (".FNT") && FontFile.isFont (exactBuffer))
            file = new FontFile (name, exactBuffer, auxType);
          else if (name.endsWith (".FONT") && FontFile.isFont (exactBuffer))
            file = new FontFile (name, exactBuffer, auxType);
          else if (ShapeTable.isShapeTable (exactBuffer))
            file = new ShapeTable (name, exactBuffer);
          else if (link != null)
          {
            if (name.endsWith (".AUX"))
              file = new DoubleHiResImage (name, link.getBuffer (), exactBuffer);
            else
              file = new DoubleHiResImage (name, exactBuffer, link.getBuffer ());
          }
          else if (name.endsWith (".PAC") || name.endsWith (".A2FC")
              || (endOfFile == 0x4000 && auxType == 0x2000))
            file = new DoubleHiResImage (name, exactBuffer);
          else if (endOfFile == 0x4000 && auxType == 0x4000)
            file = new DoubleHiResImage (name, exactBuffer);
          else if (oneOf (endOfFile, 0x1FF8, 0x1FFF, 0x2000, 0x4000)
              && oneOf (auxType, 0x1FFF, 0x2000, 0x4000, 0x6000))
            file = new OriginalHiResImage (name, exactBuffer, auxType);
          else if (endOfFile == 38400 && name.startsWith ("LVL."))
            file = new LodeRunner (name, exactBuffer);
          else if (auxType == 0x1000 && CharacterRom.isRom (exactBuffer))
            file = new CharacterRom (name, exactBuffer);
          else if (auxType == 0 && endOfFile == 0x8000)
          {
            // see gs basic disk, one of the four pictures looks ok
            file = new SHRPictureFile2 (name, exactBuffer, 0xC1, 0, endOfFile);
          }
          else
          {
            file = new AssemblerProgram (name, exactBuffer, auxType);
            if (exactBuffer.length < buffer.length)
              ((AssemblerProgram) file).setExtraBuffer (buffer, exactBuffer.length,
                  buffer.length - exactBuffer.length);
          }
          break;

        case FILE_TYPE_TEXT:
          assert auxType == 0;                        // auxType > 0 handled above
          if (name.endsWith (".S"))
            file = new MerlinSource (name, exactBuffer, auxType, endOfFile);
          else if (name.endsWith ("PLA"))
            file = new SimpleText (name, exactBuffer);
          else if (name.endsWith (".GIF") && HiResImage.isGif (exactBuffer))
            file = new OriginalHiResImage (name, exactBuffer, auxType);
          else
            file = new TextFile (name, exactBuffer, auxType, endOfFile);
          break;

        case FILE_TYPE_APPLESOFT_BASIC:
          file = new ApplesoftBasicProgram (name, exactBuffer);
          break;

        case FILE_TYPE_GS_BASIC:
          file = new BasicProgramGS (name, exactBuffer);
          break;

        case FILE_TYPE_INTEGER_BASIC:
          file = new IntegerBasicProgram (name, exactBuffer);
          break;

        case FILE_TYPE_DIRECTORY:
          VolumeDirectoryHeader vdh = parentDisk.getVolumeDirectoryHeader ();
          file = new ProdosDirectory (parentDisk, name, buffer, vdh.totalBlocks,
              vdh.freeBlocks, vdh.usedBlocks);
          break;

        case FILE_TYPE_APPLESOFT_BASIC_VARS:
          if (endOfFile == 0)
          {
            System.out.println ("Stored Variables EOF = 0");
            file = new StoredVariables (name, buffer);
          }
          else
            file = new StoredVariables (name, exactBuffer);
          break;

        case FILE_TYPE_APPLETALK:
          file = new DefaultAppleFile (name + " (Appletalk file)", buffer);
          break;

        case FILE_TYPE_GWP:
          file = new SimpleText (name, exactBuffer);
          break;

        case FILE_TYPE_AWP:
          file = new AppleworksWPFile (name + " (Appleworks Word Processor)", buffer);
          break;

        case FILE_TYPE_ADB:
          file = new AppleworksADBFile (name + " (Appleworks Database File)", buffer);
          break;

        case FILE_TYPE_ASP:
          file = new AppleworksSSFile (name + " (Appleworks Spreadsheet File)", buffer);
          break;

        case FILE_TYPE_IIGS_SOURCE:       // I think this has a resource fork
          file = new SimpleText (name, exactBuffer);
          break;

        case FILE_TYPE_IIGS_APPLICATION:
          file = new AssemblerProgram (name, buffer, auxType);
          break;

        case FILE_TYPE_IIGS_DEVICE_DRIVER:
          file = new DeviceDriver (name, exactBuffer, auxType);
          break;

        case FILE_TYPE_ICN:
          file = new IconFile (name, exactBuffer);
          break;

        case FILE_TYPE_PNT:
          if (auxType == 2)
            file = new SHRPictureFile1 (name, exactBuffer, fileType, auxType, endOfFile);
          else if (endOfFile < 0x222)
            file = new DefaultAppleFile (name, exactBuffer);
          else
            file = new SHRPictureFile2 (name, exactBuffer, fileType, auxType, endOfFile);
          break;

        case FILE_TYPE_PIC:
          file = new SHRPictureFile2 (name, exactBuffer, fileType, auxType, endOfFile);
          break;

        case FILE_TYPE_FOT:
          if (auxType == HiResImage.FADDEN_AUX)
            file = new FaddenHiResImage (name, exactBuffer, fileType, auxType, endOfFile);
          else
          {
            System.out.println ("Unwritten FOT: " + name);
            file = new DefaultAppleFile (name, exactBuffer);
          }
          break;

        case FILE_TYPE_FNT:
          file = new FontFile (name, exactBuffer, auxType);
          break;

        case FILE_TYPE_FONT:
          file = new QuickDrawFont (name, exactBuffer, fileType, auxType);
          break;

        case FILE_TYPE_DESCRIPTOR_TABLE:
          file = new FileTypeDescriptorTable (name, exactBuffer);
          break;

        case FILE_TYPE_GSOS_FILE_SYSTEM_TRANSLATOR:
          file = new FileSystemTranslator (name, exactBuffer);
          break;

        case FILE_TYPE_PASCAL_VOLUME:
        case FILE_TYPE_GEO:
        case FILE_TYPE_LDF:
        case FILE_TYPE_ANI:
        case FILE_TYPE_PAL:
        case FILE_TYPE_IIGS_OBJECT:
          file = new DefaultAppleFile (name, exactBuffer);
          break;

        case FILE_TYPE_NON:
          if (name.endsWith (".TIFF") && HiResImage.isTiff (exactBuffer))
            file = new OriginalHiResImage (name, exactBuffer, auxType);
          else
            file = new DefaultAppleFile (name, exactBuffer);
          break;

        case FILE_TYPE_FINDER:
          file = new FinderData (name, exactBuffer);
          break;

        default:
          // System.out.format ("%02X  %s  %s - Unknown Prodos file type%n",
          // fileType, fileTypes[fileType], name);
          file = new DefaultAppleFile (name, exactBuffer);
      }
    }
    catch (Exception e)
    {
      file = new ErrorMessageFile (name, buffer, e);
      e.printStackTrace ();
    }
    return file;
  }

  // ---------------------------------------------------------------------------------//
  private boolean oneOf (int val, int... values)
  // ---------------------------------------------------------------------------------//
  {
    for (int value : values)
      if (val == value)
        return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  private byte[] getExactBuffer (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    byte[] exactBuffer;
    if (buffer.length < endOfFile)
    {
      exactBuffer = new byte[endOfFile];
      System.arraycopy (buffer, 0, exactBuffer, 0, buffer.length);
    }
    else if (buffer.length == endOfFile || endOfFile == 512)    // 512 seems like crap
      exactBuffer = buffer;
    else
    {
      exactBuffer = new byte[endOfFile];
      System.arraycopy (buffer, 0, exactBuffer, 0, endOfFile);
    }
    return exactBuffer;
  }

  // ---------------------------------------------------------------------------------//
  private DataSource getRandomAccessTextFile ()
  // ---------------------------------------------------------------------------------//
  {
    // Text files with aux (reclen) > 0 are random access, possibly with
    // non-contiguous records, so they need to be handled differently

    switch (storageType)
    {
      case TREE:
        return getTreeTextFile ();
      case SAPLING:
        return getSaplingTextFile ();
      case SEEDLING:
        return getSeedlingTextFile ();
      default:
        System.out.println ("Impossible: text file: " + storageType);
        return null;
    }
  }

  // ---------------------------------------------------------------------------------//
  private DataSource getTreeTextFile ()
  // ---------------------------------------------------------------------------------//
  {
    List<TextBuffer> buffers = new ArrayList<> ();
    List<DiskAddress> addresses = new ArrayList<> ();
    int logicalBlock = 0;

    byte[] mainIndexBuffer = disk.readBlock (keyPtr);
    for (int i = 0; i < 256; i++)
    {
      int indexBlock =
          HexFormatter.intValue (mainIndexBuffer[i], mainIndexBuffer[i + 256]);
      if (indexBlock > 0)
        logicalBlock = readIndexBlock (indexBlock, addresses, buffers, logicalBlock);
      else
      {
        if (addresses.size () > 0)
        {
          byte[] tempBuffer = disk.readBlocks (addresses);
          buffers.add (
              new TextBuffer (tempBuffer, auxType, logicalBlock - addresses.size ()));
          addresses.clear ();
        }
        logicalBlock += 256;
      }
    }
    if (buffers.size () == 1 && name.endsWith (".S"))
      return new MerlinSource (name, buffers.get (0).buffer, auxType, endOfFile);

    return new TextFile (name, buffers, auxType, endOfFile);
  }

  // ---------------------------------------------------------------------------------//
  private DataSource getSaplingTextFile ()
  // ---------------------------------------------------------------------------------//
  {
    List<TextBuffer> buffers = new ArrayList<> ();
    List<DiskAddress> addresses = new ArrayList<> ();
    readIndexBlock (keyPtr, addresses, buffers, 0);

    if (buffers.size () == 1 && name.endsWith (".S"))
      return new MerlinSource (name, buffers.get (0).buffer, auxType, endOfFile);

    return new TextFile (name, buffers, auxType, endOfFile);
  }

  // ---------------------------------------------------------------------------------//
  private DataSource getSeedlingTextFile ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = getBuffer ();
    if (endOfFile < buffer.length)
    {
      byte[] exactBuffer = new byte[endOfFile];
      System.arraycopy (buffer, 0, exactBuffer, 0, endOfFile);
      buffer = exactBuffer;
    }

    if (name.endsWith (".S"))
      return new MerlinSource (name, buffer, auxType, endOfFile);

    return new TextFile (name, buffer, auxType, endOfFile);
  }

  // ---------------------------------------------------------------------------------//
  private byte[] getBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    switch (storageType)
    {
      case SEEDLING:
      case SAPLING:
      case TREE:
        return disk.readBlocks (dataBlocks);

      case SUBDIRECTORY:
        byte[] fullBuffer = new byte[dataBlocks.size () * BLOCK_ENTRY_SIZE];
        int offset = 0;
        for (DiskAddress da : dataBlocks)
        {
          byte[] buffer = disk.readBlock (da);
          System.arraycopy (buffer, 4, fullBuffer, offset, BLOCK_ENTRY_SIZE);
          offset += BLOCK_ENTRY_SIZE;
        }
        return fullBuffer;

      case GSOS_EXTENDED_FILE:
        return disk.readBlocks (dataBlocks);   // data and resource forks concatenated

      case PASCAL_ON_PROFILE:
        return disk.readBlocks (dataBlocks);

      default:
        System.out.println ("Unknown storage type in getBuffer : " + storageType);
        return new byte[512];
    }
  }

  // ---------------------------------------------------------------------------------//
  private int readIndexBlock (int indexBlock, List<DiskAddress> addresses,
      List<TextBuffer> buffers, int logicalBlock)
  // ---------------------------------------------------------------------------------//
  {
    byte[] indexBuffer = disk.readBlock (indexBlock);
    for (int j = 0; j < 256; j++)
    {
      int block = HexFormatter.intValue (indexBuffer[j], indexBuffer[j + 256]);
      if (block > 0)
        addresses.add (disk.getDiskAddress (block));
      else if (addresses.size () > 0)
      {
        byte[] tempBuffer = disk.readBlocks (addresses);
        buffers
            .add (new TextBuffer (tempBuffer, auxType, logicalBlock - addresses.size ()));
        addresses.clear ();
      }
      logicalBlock++;
    }

    return logicalBlock;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getSectors ()
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> sectors = new ArrayList<> ();
    sectors.add (catalogBlock);
    if (masterIndexBlock != null)
      sectors.add (masterIndexBlock);
    sectors.addAll (indexBlocks);
    sectors.addAll (dataBlocks);

    return sectors;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean contains (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    if (da == null)
      return false;
    if (da.equals (masterIndexBlock))
      return true;
    for (DiskAddress block : indexBlocks)
      if (da.matches (block))
        return true;
    for (DiskAddress block : dataBlocks)
      if (da.matches (block))
        return true;

    return false;
  }

  // called from ProdosDisk.processDirectoryBlock, used to link DoubleHires image files
  // ---------------------------------------------------------------------------------//
  void link (FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.link = fileEntry;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    if (ProdosConstants.fileTypes[fileType].equals ("DIR"))
      return name;

    String locked = (access == 0x00) ? "*" : " ";

    if (true)
      return String.format ("%s  %03d %s", ProdosConstants.fileTypes[fileType],
          blocksUsed, locked) + name;

    String timeC = created == null ? "" : parentDisk.df.format (created.getTime ());
    String timeF = modified == null ? "" : parentDisk.df.format (modified.getTime ());
    return String.format ("%s %s%-30s %3d %,10d %15s %15s",
        ProdosConstants.fileTypes[fileType], locked, parentDirectory.name + "/" + name,
        blocksUsed, endOfFile, timeC, timeF);
  }
}