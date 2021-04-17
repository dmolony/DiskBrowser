package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.utilities.Utility.readShort;
import static com.bytezone.diskbrowser.utilities.Utility.writeShort;

import java.io.DataInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

// -----------------------------------------------------------------------------------//
public class ProdosDisk
// -----------------------------------------------------------------------------------//
{
  static final String UNDERLINE = "------------------------------------------------\n";

  static final int BLOCK_SIZE = 512;
  private static final int CATALOG_SIZE = 4;
  static final int ENTRY_SIZE = 0x27;
  private static final int BITS_PER_BLOCK = 8 * BLOCK_SIZE;
  static final String[] storageTypes =
      { "Deleted", "Seedling", "Sapling", "Tree", "", "", "", "", "", "", "", "", "",
        "Subdirectory", "Subdirectory Header", "Volume Directory Header" };

  private BitSet volumeBitMap;
  private final int maxBlocks;
  private final byte[] buffer;
  private final byte[] bootSector = new byte[BLOCK_SIZE];

  private VolumeDirectoryHeader volumeDirectoryHeader;
  private Map<Integer, SubdirectoryHeader> subdirectoryHeaders = new HashMap<> ();

  // ---------------------------------------------------------------------------------//
  public ProdosDisk (int blocks, String volumeName) throws IOException, DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    try (DataInputStream in = new DataInputStream (ProdosDisk.class.getClassLoader ()
        .getResourceAsStream ("com/bytezone/diskbrowser/prodos/write/block-00.bin")))
    {
      int count = in.read (bootSector);
      if (count != BLOCK_SIZE)
        System.out.println ("Error with prodos boot sector");
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }

    maxBlocks = blocks;
    buffer = new byte[blocks * BLOCK_SIZE];

    volumeBitMap = new BitSet (blocks);
    for (int i = 0; i < blocks; i++)
      volumeBitMap.set (i, true);

    createCatalog (volumeName);

    volumeDirectoryHeader.write ();
    for (SubdirectoryHeader subdirectoryHeader : subdirectoryHeaders.values ())
      subdirectoryHeader.write ();
  }

  // ---------------------------------------------------------------------------------//
  void createCatalog (String volumeName) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    // reserve two boot blocks
    allocateNextBlock ();
    allocateNextBlock ();

    System.arraycopy (bootSector, 0, buffer, 0, 512);

    // write 4 catalog blocks
    for (int i = 0, prevBlockNo = 0; i < CATALOG_SIZE; i++)
    {
      int blockNo = allocateNextBlock ();
      int ptr = blockNo * BLOCK_SIZE;

      writeShort (buffer, ptr, prevBlockNo);
      writeShort (buffer, ptr + 2, 0);

      if (prevBlockNo > 0)
        writeShort (buffer, prevBlockNo * BLOCK_SIZE + 2, blockNo);

      prevBlockNo = blockNo;

      if (i == 0)
      {
        volumeDirectoryHeader = new VolumeDirectoryHeader (this, buffer, ptr + 4);
        volumeDirectoryHeader.fileName = volumeName;
        volumeDirectoryHeader.totalBlocks = maxBlocks;
        volumeDirectoryHeader.creationDate = LocalDateTime.now ();
        volumeDirectoryHeader.write ();
      }
    }

    // reserve the bitmap blocks
    int indexBlocks = (maxBlocks - 1) / BITS_PER_BLOCK + 1;
    for (int i = 0; i < indexBlocks; i++)
      allocateNextBlock ();

    writeVolumeBitMap ();
  }

  // ---------------------------------------------------------------------------------//
  public int getFreeBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return volumeBitMap.cardinality ();
  }

  // ---------------------------------------------------------------------------------//
  public FileEntry addFile (String path, byte type, int auxType, LocalDateTime created,
      LocalDateTime modified, byte[] dataBuffer) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    String[] subdirectories;
    String fileName = "";

    int pos = path.lastIndexOf ('/');
    if (pos > 0)
    {
      subdirectories = path.substring (0, pos).split ("/");
      fileName = path.substring (pos + 1);
    }
    else
    {
      subdirectories = new String[0];
      fileName = path;
    }

    int catalogBlockNo = 2;

    for (int i = 0; i < subdirectories.length; i++)
    {
      FileEntry fileEntry = searchDirectory (catalogBlockNo, subdirectories[i]);
      if (fileEntry == null)
        fileEntry = createSubdirectory (catalogBlockNo, subdirectories[i]);

      catalogBlockNo = fileEntry.keyPointer;
    }

    FileEntry fileEntry = searchDirectory (catalogBlockNo, fileName);
    if (fileEntry != null)
    {
      System.out.println ("File already exists: " + path);
      return null;          // throw something?
    }

    fileEntry = findFreeSlot (catalogBlockNo);

    if (fileEntry != null)
    {
      fileEntry.fileName = fileName;
      fileEntry.creationDate = LocalDateTime.now ();
      fileEntry.modifiedDate = LocalDateTime.now ();
      fileEntry.version = 0x00;
      fileEntry.minVersion = 0x00;
      fileEntry.headerPointer = catalogBlockNo;
      fileEntry.fileType = type;
      fileEntry.auxType = auxType;
      fileEntry.creationDate = created;
      fileEntry.modifiedDate = modified;

      fileEntry.writeFile (dataBuffer);

      fileEntry.write ();
      updateFileCount (fileEntry.headerPointer);

      return fileEntry;
    }

    return null;
  }

  // ---------------------------------------------------------------------------------//
  public void close ()
  // ---------------------------------------------------------------------------------//
  {
    writeVolumeBitMap ();
    volumeDirectoryHeader.write ();
    for (SubdirectoryHeader subdirectoryHeader : subdirectoryHeaders.values ())
      subdirectoryHeader.write ();
  }

  // ---------------------------------------------------------------------------------//
  private FileEntry searchDirectory (int blockNo, String fileName)
  // ---------------------------------------------------------------------------------//
  {
    int emptySlotPtr = 0;

    do
    {
      int offset = blockNo * BLOCK_SIZE;
      int ptr = offset + 4;
      for (int i = 0; i < 13; i++)
      {
        int storageTypeNameLength = buffer[ptr] & 0xFF;
        if (storageTypeNameLength == 0)
        {
          if (emptySlotPtr == 0)
            emptySlotPtr = ptr;
        }
        else
        {
          int nameLength = buffer[ptr] & 0x0F;

          String entryName = new String (buffer, ptr + 1, nameLength);
          if (entryName.equals (fileName))
          {
            FileEntry fileEntry = new FileEntry (this, buffer, ptr);
            fileEntry.read ();
            return fileEntry;
          }
        }

        ptr += ENTRY_SIZE;
      }
      blockNo = readShort (buffer, offset + 2);
    } while (blockNo > 0);

    return null;
  }

  // ---------------------------------------------------------------------------------//
  private FileEntry createSubdirectory (int blockNo, String name) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    FileEntry fileEntry = findFreeSlot (blockNo);

    if (fileEntry != null)
    {
      fileEntry.storageType = 0x0D;                     // subdirectory
      fileEntry.fileName = name;
      fileEntry.keyPointer = allocateNextBlock ();
      fileEntry.blocksUsed = 1;
      fileEntry.eof = 512;
      fileEntry.fileType = 0x0F;                        // DIR
      fileEntry.headerPointer = blockNo;
      fileEntry.creationDate = LocalDateTime.now ();
      fileEntry.modifiedDate = LocalDateTime.now ();

      fileEntry.write ();

      updateFileCount (fileEntry.headerPointer);

      SubdirectoryHeader subdirectoryHeader =
          new SubdirectoryHeader (this, buffer, fileEntry.keyPointer * BLOCK_SIZE + 4);
      subdirectoryHeader.fileName = name;
      subdirectoryHeader.creationDate = LocalDateTime.now ();
      subdirectoryHeader.fileCount = 0;
      subdirectoryHeader.parentPointer = (byte) blockNo;
      subdirectoryHeader.parentEntry =
          (byte) (((fileEntry.ptr % BLOCK_SIZE) - 4) / ENTRY_SIZE + 1);

      subdirectoryHeader.write ();

      subdirectoryHeaders.put (fileEntry.keyPointer, subdirectoryHeader);

      return fileEntry;
    }

    System.out.println ("failed");

    return null;          // no empty slots found
  }

  // ---------------------------------------------------------------------------------//
  private void updateFileCount (int catalogBlock)
  // ---------------------------------------------------------------------------------//
  {
    if (catalogBlock == 2)
    {
      volumeDirectoryHeader.fileCount++;
      volumeDirectoryHeader.write ();
    }
    else
    {
      SubdirectoryHeader subdirectoryHeader = subdirectoryHeaders.get (catalogBlock);
      subdirectoryHeader.fileCount++;
      subdirectoryHeader.write ();
    }
  }

  // ---------------------------------------------------------------------------------//
  int allocateNextBlock () throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    int nextBlock = getFreeBlock ();
    if (nextBlock < 0)
      throw new DiskFullException ("Disk Full");

    volumeBitMap.set (nextBlock, false);      // mark as unavailable

    return nextBlock;
  }

  // ---------------------------------------------------------------------------------//
  private int getFreeBlock ()
  // ---------------------------------------------------------------------------------//
  {
    return volumeBitMap.nextSetBit (0);
  }

  // ---------------------------------------------------------------------------------//
  private FileEntry findFreeSlot (int blockNo) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    SubdirectoryHeader subdirectoryHeader = subdirectoryHeaders.get (blockNo);
    int lastBlockNo = 0;

    do
    {
      int offset = blockNo * BLOCK_SIZE;
      int ptr = offset + 4;

      for (int i = 0; i < 13; i++)
      {
        if (buffer[ptr] == 0)         // free slot
          return new FileEntry (this, buffer, ptr);

        ptr += ENTRY_SIZE;
      }

      lastBlockNo = blockNo;
      blockNo = readShort (buffer, offset + 2);      // next block
    } while (blockNo > 0);

    // no free slots, so add a new catalog block
    blockNo = allocateNextBlock ();

    // update file entry size
    FileEntry fileEntry =
        new FileEntry (this, buffer, subdirectoryHeader.parentPointer * BLOCK_SIZE
            + (subdirectoryHeader.parentEntry - 1) * ENTRY_SIZE + 4);
    fileEntry.read ();
    fileEntry.blocksUsed++;
    fileEntry.eof += BLOCK_SIZE;
    fileEntry.modifiedDate = LocalDateTime.now ();
    fileEntry.write ();

    // update links
    int ptr = blockNo * BLOCK_SIZE;
    writeShort (buffer, lastBlockNo * BLOCK_SIZE + 2, blockNo);   // point to next block
    writeShort (buffer, ptr, lastBlockNo);                        // point to previous block

    return new FileEntry (this, buffer, ptr + 4);      // first slot in new block
  }

  // ---------------------------------------------------------------------------------//
  private void writeVolumeBitMap ()
  // ---------------------------------------------------------------------------------//
  {
    int ptr = (2 + CATALOG_SIZE) * BLOCK_SIZE;
    int val = 0;
    int blockNo = 0;

    while (blockNo < maxBlocks)
    {
      val = val << 1;
      if (volumeBitMap.get (blockNo++))
        val |= 1;

      if (blockNo % 8 == 0)
      {
        buffer[ptr++] = (byte) val;
        val = 0;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    return buffer;
  }
}
