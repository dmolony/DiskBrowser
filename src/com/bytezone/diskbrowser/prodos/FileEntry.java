package com.bytezone.diskbrowser.prodos;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.bytezone.diskbrowser.applefile.*;
import com.bytezone.diskbrowser.appleworks.AppleworksADBFile;
import com.bytezone.diskbrowser.appleworks.AppleworksSSFile;
import com.bytezone.diskbrowser.appleworks.AppleworksWPFile;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// - Set sector types for each used sector
// - Populate dataBlocks, indexBlocks, catalogBlock and masterIndexBlock
// - Provide getDataSource ()

class FileEntry extends CatalogEntry implements ProdosConstants
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
  private final List<DiskAddress> indexBlocks = new ArrayList<DiskAddress> ();
  private boolean invalid;
  private FileEntry link;

  public FileEntry (ProdosDisk fDisk, byte[] entryBuffer, DirectoryHeader parent,
      int parentBlock)
  {
    super (fDisk, entryBuffer);
    assert parent != null;
    this.parentDirectory = parent;
    this.catalogBlock = this.disk.getDiskAddress (parentBlock);

    fileType = entryBuffer[16] & 0xFF;
    keyPtr = HexFormatter.intValue (entryBuffer[17], entryBuffer[18]);
    blocksUsed = HexFormatter.intValue (entryBuffer[19], entryBuffer[20]);
    endOfFile = HexFormatter.intValue (entryBuffer[21], entryBuffer[22], entryBuffer[23]);

    auxType = HexFormatter.intValue (entryBuffer[31], entryBuffer[32]);
    modified = HexFormatter.getAppleDate (entryBuffer, 33);
    headerPointer = HexFormatter.intValue (entryBuffer[37], entryBuffer[38]);

    switch (storageType)
    {
      case TYPE_SEEDLING:
        parentDisk.setSectorType (keyPtr, fDisk.dataSector);
        DiskAddress da = disk.getDiskAddress (keyPtr);
        if (da != null)
          dataBlocks.add (da);
        else
          invalid = true;
        break;

      case TYPE_SAPLING:
        if (isGEOSFile ())
          traverseGEOSIndex (keyPtr);
        else
          traverseIndex (keyPtr);
        break;

      case TYPE_TREE:
        parentDisk.setSectorType (keyPtr, fDisk.masterIndexSector);
        masterIndexBlock = disk.getDiskAddress (keyPtr);
        if (isGEOSFile ())
          traverseGEOSMasterIndex (keyPtr);
        else
          traverseMasterIndex (keyPtr);
        break;

      case TYPE_GSOS_EXTENDED_FILE:
        parentDisk.setSectorType (keyPtr, fDisk.extendedKeySector);
        indexBlocks.add (disk.getDiskAddress (keyPtr));
        byte[] buffer2 = disk.readSector (keyPtr);        // data fork and resource fork

        for (int i = 0; i < 512; i += 256)
        {
          int storageType = buffer2[i] & 0x0F;
          int keyBlock = HexFormatter.intValue (buffer2[i + 1], buffer2[i + 2]);
          switch (storageType)
          {
            case ProdosConstants.TYPE_SEEDLING:
              parentDisk.setSectorType (keyBlock, fDisk.dataSector);
              dataBlocks.add (disk.getDiskAddress (keyBlock));
              break;
            case ProdosConstants.TYPE_SAPLING:
              traverseIndex (keyBlock);
              break;
            case ProdosConstants.TYPE_TREE:
              traverseMasterIndex (keyBlock);
              break;
            default:
              System.out.println ("fork not a tree, sapling or seedling!!!");
          }
        }
        break;

      case TYPE_SUBDIRECTORY:
        int block = keyPtr;
        do
        {
          dataBlocks.add (disk.getDiskAddress (block));
          byte[] buffer = disk.readSector (block);
          block = HexFormatter.intValue (buffer[2], buffer[3]);
        } while (block > 0);
        break;

      default:
        System.out.println ("Unknown storage type: " + storageType);
    }
  }

  private boolean isGEOSFile ()
  {
    return ((fileType & 0xF0) == 0x80);
  }

  private void removeEmptyBlocks ()
  {
    while (dataBlocks.size () > 0)
    {
      DiskAddress da = dataBlocks.get (dataBlocks.size () - 1);
      if (da.getBlock () == 0)
        dataBlocks.remove (dataBlocks.size () - 1);
      else
        break;
    }
  }

  private void traverseMasterIndex (int keyPtr)
  {
    byte[] buffer = disk.readSector (keyPtr); // master index
    // find the last used index block
    // get the file size from the catalog and only check those blocks
    int highestBlock = 0;
    // A master index block can never be more than half full
    for (int i = 127; i >= 0; i--)
    {
      int block = HexFormatter.intValue (buffer[i], buffer[i + 256]);
      if (block > 0)
      {
        highestBlock = i;
        break;
      }
    }
    for (int i = 0; i <= highestBlock; i++)
    {
      int block = HexFormatter.intValue (buffer[i], buffer[i + 256]); // index
      if (block != 0)
        traverseIndex (block);
      else
      // add 256 empty data blocks
      {
        DiskAddress da = disk.getDiskAddress (0);
        for (int j = 0; j < 256; j++)
          dataBlocks.add (da);
      }
    }
    removeEmptyBlocks ();
  }

  private void traverseIndex (int keyBlock)
  {
    parentDisk.setSectorType (keyBlock, parentDisk.indexSector);
    indexBlocks.add (disk.getDiskAddress (keyBlock));
    byte[] buffer = disk.readSector (keyBlock);
    for (int i = 0; i < 256; i++)
    {
      int block = HexFormatter.intValue (buffer[i], buffer[i + 256]);
      if (!disk.isValidAddress (block))
      {
        System.out.println ("Invalid block in " + name + " : " + block);
        invalid = true;
        break;
      }
      // System.out.printf ("%4d  %02X  %02X%n", block, fileType, auxType);
      // should we break if block == 0 and it's not a text file?
      // if (block == 0 && !(fileType == ProdosConstants.FILE_TYPE_TEXT && auxType > 0))
      // if (block == 0 && fileType != 4)
      // break;
      if (block != 0)
      {
        parentDisk.setSectorType (block, parentDisk.dataSector);
        dataBlocks.add (disk.getDiskAddress (block));
      }
    }
  }

  private void traverseGEOSMasterIndex (int keyPtr)
  {
    byte[] buffer = disk.readSector (keyPtr); // master index
    // int length = HexFormatter.intValue (buffer[0xFF], buffer[0x1FF]);
    for (int i = 0; i < 0x80; i++)
    {
      int block = HexFormatter.intValue (buffer[i], buffer[i + 256]);
      if (block == 0)
        break;
      if (block == 0xFFFF)
        continue;
      traverseGEOSIndex (block);
    }
  }

  private void traverseGEOSIndex (int keyPtr)
  {
    parentDisk.setSectorType (keyPtr, parentDisk.indexSector);
    indexBlocks.add (disk.getDiskAddress (keyPtr));
    byte[] buffer = disk.readSector (keyPtr);
    // int length = HexFormatter.intValue (buffer[0xFF], buffer[0x1FF]);
    for (int i = 0; i < 0x80; i++)
    {
      int block = HexFormatter.intValue (buffer[i], buffer[i + 256]);
      if (block == 0)
        break;
      if (block == 0xFFFF)
        continue;
      parentDisk.setSectorType (block, parentDisk.dataSector);
      dataBlocks.add (disk.getDiskAddress (block));
    }
  }

  @Override
  public DataSource getDataSource ()
  {
    if (file != null)
      return file;
    if (invalid)
    {
      file = new DefaultAppleFile (name, null);
      return file;
    }
    /*
     * Text files with reclen > 0 are random access, possibly with gaps between
     * records, so they need to be handled separately.
     */
    if (fileType == FILE_TYPE_TEXT && auxType > 0)
    {
      switch (storageType)
      {
        case TYPE_TREE:
          return getTreeTextFile ();
        case TYPE_SAPLING:
          return getSaplingTextFile ();
        case TYPE_SEEDLING:
          return getSeedlingTextFile ();
      }
    }

    byte[] buffer = isGEOSFile () ? getGEOSBuffer () : getBuffer ();
    byte[] exactBuffer = getExactBuffer (buffer);

    try
    {
      switch (fileType)
      {
        case FILE_TYPE_BINARY:
        case FILE_TYPE_RELOCATABLE:
        case FILE_TYPE_SYS:
          //          if (name.endsWith (".S"))
          //            file = new MerlinSource (name, exactBuffer, auxType, endOfFile);
          if (ShapeTable.isShapeTable (exactBuffer))
            file = new ShapeTable (name, exactBuffer);
          else if (SimpleText.isHTML (exactBuffer))
            file = new SimpleText (name, exactBuffer);
          else if (HiResImage.isGif (exactBuffer))
            file = new OriginalHiResImage (name, exactBuffer, auxType);
          else if (link != null)
          {
            if (name.endsWith (".AUX"))
              file = new DoubleHiResImage (name, link.getBuffer (), exactBuffer);
            else
              file = new DoubleHiResImage (name, exactBuffer, link.getBuffer ());
          }
          else if (name.endsWith (".PAC") || name.endsWith (".A2FC"))
            file = new DoubleHiResImage (name, exactBuffer);
          else if ((endOfFile == 0x1FF8 || endOfFile == 0x1FFF || endOfFile == 0x2000
              || endOfFile == 0x4000)
              && (auxType == 0x1FFF || auxType == 0x2000 || auxType == 0x4000))
            file = new OriginalHiResImage (name, exactBuffer, auxType);
          else if (endOfFile == 38400 && name.startsWith ("LVL."))
            file = new LodeRunner (name, exactBuffer);
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
          else
            file = new TextFile (name, exactBuffer, auxType, endOfFile);
          break;
        case FILE_TYPE_APPLESOFT_BASIC:
          file = new BasicProgram (name, exactBuffer);
          break;
        case FILE_TYPE_INTEGER_BASIC:
          file = new IntegerBasicProgram (name, exactBuffer);
          break;
        case FILE_TYPE_DIRECTORY:
          VolumeDirectoryHeader vdh = parentDisk.vdh;
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
          if (auxType == 1)
            file = new PackedSHR (name, exactBuffer, fileType, auxType);
          if (auxType == 2)
            file = new SHRPictureFile (name, exactBuffer, fileType, auxType);
          else
            file = new OriginalHiResImage (name, exactBuffer, fileType, auxType);
          break;
        case FILE_TYPE_PIC:
          file = new OriginalHiResImage (name, exactBuffer, fileType, auxType);
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
        default:
          System.out.format ("Unknown file type : %02X%n", fileType);
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

  private byte[] getExactBuffer (byte[] buffer)
  {
    if (buffer.length < endOfFile)
      System.out.printf ("Buffer (%,d) shorter than EOF (%,d) in %s%n", buffer.length,
          endOfFile, name);

    byte[] exactBuffer;
    if (buffer.length < endOfFile)
    {
      exactBuffer = new byte[buffer.length];
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

  private DataSource getTreeTextFile ()
  {
    List<TextBuffer> buffers = new ArrayList<TextBuffer> ();
    List<DiskAddress> addresses = new ArrayList<DiskAddress> ();
    int logicalBlock = 0;

    byte[] mainIndexBuffer = disk.readSector (keyPtr);
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
          byte[] tempBuffer = disk.readSectors (addresses);
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

  private DataSource getSaplingTextFile ()
  {
    List<TextBuffer> buffers = new ArrayList<TextBuffer> ();
    List<DiskAddress> addresses = new ArrayList<DiskAddress> ();
    readIndexBlock (keyPtr, addresses, buffers, 0);
    if (buffers.size () == 1 && name.endsWith (".S"))
      return new MerlinSource (name, buffers.get (0).buffer, auxType, endOfFile);
    return new TextFile (name, buffers, auxType, endOfFile);
  }

  private DataSource getSeedlingTextFile ()
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

  private byte[] getBuffer ()
  {
    switch (storageType)
    {
      case TYPE_SEEDLING:
      case TYPE_SAPLING:
      case TYPE_TREE:
        return disk.readSectors (dataBlocks);
      case TYPE_GSOS_EXTENDED_FILE:
        // this will return the data fork and the resource fork concatenated
        return disk.readSectors (dataBlocks);
      case TYPE_SUBDIRECTORY:
        byte[] fullBuffer = new byte[dataBlocks.size () * BLOCK_ENTRY_SIZE]; // 39 * 13 = 507
        int offset = 0;
        for (DiskAddress da : dataBlocks)
        {
          byte[] buffer = disk.readSector (da);
          System.arraycopy (buffer, 4, fullBuffer, offset, BLOCK_ENTRY_SIZE);
          offset += BLOCK_ENTRY_SIZE;
        }
        return fullBuffer;
      default:
        System.out.println ("Unknown storage type in getBuffer : " + storageType);
        return new byte[512];
    }
  }

  private byte[] getGEOSBuffer ()
  {
    switch (storageType)
    {
      case TYPE_SEEDLING:
        System.out.println ("Seedling GEOS file : " + name); // not sure if this is possible
        return disk.readSectors (dataBlocks);
      case TYPE_SAPLING:
        return getIndexFile (keyPtr);
      case TYPE_TREE:
        return getMasterIndexFile (keyPtr);
      default:
        System.out.println ("Unknown storage type for GEOS file : " + storageType);
        return new byte[512];
    }
  }

  private byte[] getMasterIndexFile (int keyPtr)
  {
    byte[] buffer = disk.readSector (keyPtr);
    int length = HexFormatter.intValue (buffer[0xFF], buffer[0x1FF]);
    byte[] fileBuffer = new byte[length];
    int ptr = 0;
    for (int i = 0; i < 0x80; i++)
    {
      int block = HexFormatter.intValue (buffer[i], buffer[i + 256]);
      if (block == 0)
        break;
      if (block == 0xFFFF) // should this insert 131,072 zeroes?
        continue;
      byte[] temp = getIndexFile (block);
      System.arraycopy (temp, 0, fileBuffer, ptr, temp.length);
      ptr += temp.length;
    }
    return fileBuffer;
  }

  private byte[] getIndexFile (int keyPtr)
  {
    byte[] buffer = disk.readSector (keyPtr);
    int length = HexFormatter.intValue (buffer[0xFF], buffer[0x1FF]);
    byte[] fileBuffer = new byte[length];
    for (int i = 0; i < 0x80; i++)
    {
      int block = HexFormatter.intValue (buffer[i], buffer[i + 256]);
      if (block == 0)
        break;
      if (block == 0xFFFF) // should this insert 512 zeroes?
        continue;
      byte[] temp = disk.readSector (block);
      System.arraycopy (temp, 0, fileBuffer, i * 512, length > 512 ? 512 : length);
      length -= 512;
    }
    return fileBuffer;
  }

  private int readIndexBlock (int indexBlock, List<DiskAddress> addresses,
      List<TextBuffer> buffers, int logicalBlock)
  {
    byte[] indexBuffer = disk.readSector (indexBlock);
    for (int j = 0; j < 256; j++)
    {
      int block = HexFormatter.intValue (indexBuffer[j], indexBuffer[j + 256]);
      if (block > 0)
        addresses.add (disk.getDiskAddress (block));
      else if (addresses.size () > 0)
      {
        byte[] tempBuffer = disk.readSectors (addresses);
        buffers
            .add (new TextBuffer (tempBuffer, auxType, logicalBlock - addresses.size ()));
        addresses.clear ();
      }
      logicalBlock++;
    }
    return logicalBlock;
  }

  @Override
  public List<DiskAddress> getSectors ()
  {
    List<DiskAddress> sectors = new ArrayList<DiskAddress> ();
    sectors.add (catalogBlock);
    if (masterIndexBlock != null)
      sectors.add (masterIndexBlock);
    sectors.addAll (indexBlocks);
    sectors.addAll (dataBlocks);
    return sectors;
  }

  @Override
  public boolean contains (DiskAddress da)
  {
    if (da.equals (masterIndexBlock))
      return true;
    for (DiskAddress block : indexBlocks)
      if (block.matches (da))
        return true;
    for (DiskAddress block : dataBlocks)
      if (block.matches (da))
        return true;
    return false;
  }

  void link (FileEntry fileEntry)
  {
    this.link = fileEntry;
  }

  @Override
  public String toString ()
  {
    if (ProdosConstants.fileTypes[fileType].equals ("DIR"))
      return name;
    // String locked = (access == 0x01) ? "*" : " ";
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