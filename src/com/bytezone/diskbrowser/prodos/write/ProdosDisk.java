package com.bytezone.diskbrowser.prodos.write;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  private static byte[] dummySeedling = new byte[50];
  private static byte[] dummySapling = new byte[2530];
  private static byte[] dummyTree = new byte[132000];

  private BitSet volumeBitMap;
  private final int maxBlocks;
  private final byte[] buffer;
  private final byte[] bootSector = new byte[512];

  private VolumeDirectoryHeader volumeDirectoryHeader;
  private Map<Integer, SubdirectoryHeader> subdirectoryHeaders = new HashMap<> ();

  // ---------------------------------------------------------------------------------//
  public ProdosDisk (int blocks, String volumeName) throws IOException
  // ---------------------------------------------------------------------------------//
  {
    try (DataInputStream in = new DataInputStream (ProdosDisk.class.getClassLoader ()
        .getResourceAsStream ("com/bytezone/prodos/block-00.bin")))
    {
      int count = in.read (bootSector);
      if (count != 512)
        System.out.println ("Error with prodos boot sector");
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }

    setBuffer (dummySeedling, "This is a seedling file.~");
    setBuffer (dummySapling, "This is a sapling file.~It could be a bit longer.~");
    setBuffer (dummyTree, "This is a tree file.~It is a bit short though.~");

    maxBlocks = blocks;
    buffer = new byte[blocks * BLOCK_SIZE];

    volumeBitMap = new BitSet (blocks);
    for (int i = 0; i < blocks; i++)
      volumeBitMap.set (i, true);

    createCatalog (volumeName);

    for (int i = 1; i < 4; i++)
      test (String.format ("FRED/MARY/FILE%02d.TXT", i), i);
    for (int i = 4; i <= 4; i++)
      test (String.format ("BOB/JANE/TMART%02d.TXT", i), i);

    writeVolumeBitMap ();

    volumeDirectoryHeader.write ();
    for (SubdirectoryHeader subdirectoryHeader : subdirectoryHeaders.values ())
      subdirectoryHeader.write ();
  }

  // ---------------------------------------------------------------------------------//
  void createCatalog (String volumeName)
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
  public void saveDisk (Path path)
  // ---------------------------------------------------------------------------------//
  {
    //    if (Files.exists (path))
    //    {
    //      System.out.println ("File already exists: " + path);
    //      return;
    //    }

    try
    {
      Files.write (path, buffer);
    }
    catch (IOException ex)
    {
      ex.printStackTrace ();
    }
  }

  // ---------------------------------------------------------------------------------//
  public int getFreeBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return volumeBitMap.cardinality ();
  }

  // ---------------------------------------------------------------------------------//
  void test (String path, int type)
  // ---------------------------------------------------------------------------------//
  {
    String[] paths;
    String fileName = "";
    int pos = path.lastIndexOf ('/');
    if (pos > 0)
    {
      fileName = path.substring (pos + 1);
      path = path.substring (0, pos);
      paths = path.split ("/");
    }
    else
    {
      fileName = path;
      paths = new String[0];
    }

    int catalogBlock = 2;

    for (int i = 0; i < paths.length; i++)
    {
      String name = paths[i];
      FileEntry fileEntry = searchDirectory (catalogBlock, name);
      if (fileEntry == null)
      {
        FileEntry fileEntry2 = createSubdirectory (catalogBlock, name);
        catalogBlock = fileEntry2.keyPointer;
      }
      else
        catalogBlock = fileEntry.keyPointer;
    }

    FileEntry fileEntry = searchDirectory (catalogBlock, fileName);
    if (fileEntry == null)
      fileEntry = createFileEntry (catalogBlock, fileName, type);
  }

  // ---------------------------------------------------------------------------------//
  FileEntry createFileEntry (int blockNo, String name, int type)
  // ---------------------------------------------------------------------------------//
  {
    FileEntry fileEntry = findFreeSlot (blockNo);

    if (fileEntry != null)
    {
      fileEntry.fileName = name;
      fileEntry.creationDate = LocalDateTime.now ();
      fileEntry.modifiedDate = LocalDateTime.now ();
      fileEntry.version = 0x00;
      fileEntry.minVersion = 0x00;
      fileEntry.headerPointer = blockNo;
      fileEntry.fileType = 4;                   // text

      switch (type)
      {
        case 1:
          fileEntry.writeFile (dummySeedling);
          break;

        case 2:
          fileEntry.writeFile (dummySapling);
          break;

        case 3:
          fileEntry.writeFile (dummyTree);
          break;

        case 4:
          fileEntry.auxType = 60;       // record length
          fileEntry.writeRecord (17, getBuffer (String.format ("Record: %5d~", 17)));
          fileEntry.writeRecord (45, getBuffer (String.format ("Record: %5d~", 45)));
          fileEntry.writeRecord (50, getBuffer (String.format ("Record: %5d~", 50)));
          fileEntry.writeRecord (51, getBuffer (String.format ("Record: %5d~", 51)));
          fileEntry.writeRecord (500, getBuffer (String.format ("Record: %5d~", 500)));
          fileEntry.writeRecord (2000, getBuffer (String.format ("Record: %5d~", 2000)));
          fileEntry.writeRecord (3000, getBuffer (String.format ("Record: %5d~", 3000)));
          break;
      }

      fileEntry.write ();
      updateFileCount (fileEntry.headerPointer);

      return fileEntry;
    }

    return null;
  }

  // ---------------------------------------------------------------------------------//
  private byte[] getBuffer (String text)
  // ---------------------------------------------------------------------------------//
  {
    byte[] dataBuffer = text.getBytes ();
    for (int i = 0; i < dataBuffer.length; i++)
      if (dataBuffer[i] == '~')
        dataBuffer[i] = 0x0D;
      else
        dataBuffer[i] |= 0x80;

    return dataBuffer;
  }

  // ---------------------------------------------------------------------------------//
  private void setBuffer (byte[] buffer, String text)
  // ---------------------------------------------------------------------------------//
  {
    byte[] dataBuffer = text.getBytes ();
    for (int i = 0; i < dataBuffer.length; i++)
      if (dataBuffer[i] == '~')
        buffer[i] = 0x0D;
      else
        buffer[i] = (byte) (dataBuffer[i] | 0x80);
    buffer[dataBuffer.length - 1] = 0x0D;
  }

  // ---------------------------------------------------------------------------------//
  FileEntry searchDirectory (int blockNo, String fileName)
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
      blockNo = ProdosDisk.readShort (buffer, offset + 2);
    } while (blockNo > 0);

    return null;
  }

  // ---------------------------------------------------------------------------------//
  FileEntry createSubdirectory (int blockNo, String name)
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
  void updateFileCount (int catalogBlock)
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
  int allocateNextBlock ()
  // ---------------------------------------------------------------------------------//
  {
    int nextBlock = getFreeBlock ();
    volumeBitMap.set (nextBlock, false);      // mark as unavailable

    return nextBlock;
  }

  // ---------------------------------------------------------------------------------//
  int getFreeBlock ()
  // ---------------------------------------------------------------------------------//
  {
    return volumeBitMap.nextSetBit (0);
  }

  // ---------------------------------------------------------------------------------//
  String getPrefix (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    int pos = fileName.indexOf ('/', 1);
    if (pos < 0)
      return fileName;
    return fileName.substring (0, pos);
  }

  // ---------------------------------------------------------------------------------//
  FileEntry findFreeSlot (int blockNo)
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
      blockNo = ProdosDisk.readShort (buffer, offset + 2);      // next block
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
  void writeVolumeBitMap ()
  // ---------------------------------------------------------------------------------//
  {
    int ptr = (2 + CATALOG_SIZE) * BLOCK_SIZE;
    //    int count = 0;
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
  public static LocalDateTime getAppleDate (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    int yymmdd = readShort (buffer, offset);
    if (yymmdd != 0)
    {
      int year = (yymmdd & 0xFE00) >> 9;
      int month = (yymmdd & 0x01E0) >> 5;
      int day = yymmdd & 0x001F;

      int minute = buffer[offset + 2] & 0x3F;
      int hour = buffer[offset + 3] & 0x1F;

      if (year < 70)
        year += 2000;
      else
        year += 1900;
      return LocalDateTime.of (year, month - 1, day, hour, minute);
    }
    return null;
  }

  // ---------------------------------------------------------------------------------//
  public static void putAppleDate (byte[] buffer, int offset, LocalDateTime date)
  // ---------------------------------------------------------------------------------//
  {
    if (date == null)
    {
      System.out.println ("ignoring null date");
    }
    else
    {
      int year = date.getYear ();
      int month = date.getMonthValue ();
      int day = date.getDayOfMonth ();
      int hour = date.getHour ();
      int minute = date.getMinute ();

      if (year < 2000)
        year -= 1900;
      else
        year -= 2000;

      int val1 = year << 9 | month << 5 | day;
      writeShort (buffer, offset, val1);
      buffer[offset + 2] = (byte) minute;
      buffer[offset + 3] = (byte) hour;
    }
  }

  // ---------------------------------------------------------------------------------//
  static void writeShort (byte[] buffer, int ptr, int value)
  // ---------------------------------------------------------------------------------//
  {
    buffer[ptr] = (byte) (value & 0xFF);
    buffer[ptr + 1] = (byte) ((value & 0xFF00) >>> 8);
  }

  // ---------------------------------------------------------------------------------//
  static void writeTriple (byte[] buffer, int ptr, int value)
  // ---------------------------------------------------------------------------------//
  {
    buffer[ptr] = (byte) (value & 0xFF);
    buffer[ptr + 1] = (byte) ((value & 0xFF00) >>> 8);
    buffer[ptr + 2] = (byte) ((value & 0xFF0000) >>> 16);
  }

  // ---------------------------------------------------------------------------------//
  static int readShort (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return buffer[ptr] | buffer[ptr + 1] << 8;
  }

  // ---------------------------------------------------------------------------------//
  static int readTriple (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return buffer[ptr] | buffer[ptr + 1] << 8 | buffer[ptr + 2] << 16;
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args) throws IOException
  // ---------------------------------------------------------------------------------//
  {
    String base = System.getProperty ("user.home");
    Path path = Paths.get (base + "/Dropbox/Examples/Testing/Test01.po");
    ProdosDisk disk = new ProdosDisk (1600, "DENIS.DISK");
    disk.saveDisk (path);
  }
}
