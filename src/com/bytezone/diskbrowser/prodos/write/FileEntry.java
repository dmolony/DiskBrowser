package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.BLOCK_SIZE;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.ENTRY_SIZE;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.SAPLING;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.SEEDLING;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.TREE;
import static com.bytezone.diskbrowser.prodos.write.ProdosDisk.UNDERLINE;
import static com.bytezone.diskbrowser.utilities.Utility.getAppleDate;
import static com.bytezone.diskbrowser.utilities.Utility.putAppleDate;
import static com.bytezone.diskbrowser.utilities.Utility.readShort;
import static com.bytezone.diskbrowser.utilities.Utility.readTriple;
import static com.bytezone.diskbrowser.utilities.Utility.writeShort;
import static com.bytezone.diskbrowser.utilities.Utility.writeTriple;

import java.time.LocalDateTime;

// -----------------------------------------------------------------------------------//
public class FileEntry
// -----------------------------------------------------------------------------------//
{
  private final ProdosDisk disk;
  private final byte[] buffer;
  private final int ptr;

  String fileName;
  byte storageType;
  LocalDateTime creationDate;
  LocalDateTime modifiedDate;
  byte fileType;
  int keyPointer;
  int blocksUsed;
  int eof;
  byte version = 0x00;
  byte minVersion = 0x00;
  byte access = (byte) 0xE3;
  int auxType;
  int headerPointer;

  private IndexBlock indexBlock = null;
  private MasterIndexBlock masterIndexBlock = null;

  // ---------------------------------------------------------------------------------//
  public FileEntry (ProdosDisk disk, byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    this.disk = disk;
    this.buffer = buffer;
    this.ptr = ptr;
  }

  // ---------------------------------------------------------------------------------//
  int getBlockNo ()
  // ---------------------------------------------------------------------------------//
  {
    return ptr / BLOCK_SIZE;
  }

  // ---------------------------------------------------------------------------------//
  int getEntryNo ()
  // ---------------------------------------------------------------------------------//
  {
    return (((ptr % BLOCK_SIZE) - 4) / ENTRY_SIZE + 1);
  }

  // ---------------------------------------------------------------------------------//
  void read ()
  // ---------------------------------------------------------------------------------//
  {
    storageType = (byte) ((buffer[ptr] & 0xF0) >>> 4);

    int nameLength = buffer[ptr] & 0x0F;
    if (nameLength > 0)
      fileName = new String (buffer, ptr + 1, nameLength);
    else
      fileName = "";

    fileType = buffer[ptr + 0x10];
    keyPointer = readShort (buffer, ptr + 0x11);
    blocksUsed = readShort (buffer, ptr + 0x13);
    eof = readTriple (buffer, ptr + 0x15);
    creationDate = getAppleDate (buffer, ptr + 0x18);

    version = buffer[ptr + 0x1C];
    minVersion = buffer[ptr + 0x1D];
    access = buffer[ptr + 0x1E];

    auxType = readShort (buffer, ptr + 0x1F);
    modifiedDate = getAppleDate (buffer, ptr + 0x21);
    headerPointer = readShort (buffer, ptr + 0x25);
  }

  // ---------------------------------------------------------------------------------//
  void write ()
  // ---------------------------------------------------------------------------------//
  {
    buffer[ptr] = (byte) ((storageType << 4) | fileName.length ());
    System.arraycopy (fileName.getBytes (), 0, buffer, ptr + 1, fileName.length ());

    buffer[ptr + 0x10] = fileType;
    writeShort (buffer, ptr + 0x11, keyPointer);
    writeShort (buffer, ptr + 0x13, blocksUsed);
    writeTriple (buffer, ptr + 0x15, eof);
    putAppleDate (buffer, ptr + 0x18, creationDate);

    buffer[ptr + 0x1C] = version;
    buffer[ptr + 0x1D] = minVersion;
    buffer[ptr + 0x1E] = access;

    writeShort (buffer, ptr + 0x1F, auxType);
    putAppleDate (buffer, ptr + 0x21, modifiedDate);
    writeShort (buffer, ptr + 0x25, headerPointer);
  }

  // ---------------------------------------------------------------------------------//
  void writeFile (byte[] dataBuffer) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    this.eof = dataBuffer.length;

    int dataPtr = 0;
    int remaining = eof;

    while (dataPtr < eof)
    {
      int actualBlockNo = allocateNextBlock ();
      map (dataPtr / BLOCK_SIZE, actualBlockNo);

      int bufferPtr = actualBlockNo * BLOCK_SIZE;
      int tfr = Math.min (remaining, BLOCK_SIZE);

      System.arraycopy (dataBuffer, dataPtr, buffer, bufferPtr, tfr);

      dataPtr += BLOCK_SIZE;
      remaining -= BLOCK_SIZE;
    }

    writeIndices ();
  }

  // ---------------------------------------------------------------------------------//
  void writeRecord (int recordNo, byte[] dataBuffer) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    assert auxType > 0;           // record length

    int destPtr = auxType * recordNo;
    int remaining = Math.min (auxType, dataBuffer.length);
    int max = destPtr + remaining;
    int dataPtr = 0;

    if (eof < max)
      eof = max;

    while (destPtr < max)
    {
      int logicalBlockNo = destPtr / BLOCK_SIZE;
      int blockOffset = destPtr % BLOCK_SIZE;
      int tfr = Math.min (BLOCK_SIZE - blockOffset, remaining);

      int actualBlockNo = getActualBlockNo (logicalBlockNo);
      int bufferPtr = actualBlockNo * BLOCK_SIZE + blockOffset;

      System.arraycopy (dataBuffer, dataPtr, buffer, bufferPtr, tfr);

      //      System.out.printf ("%7d  %5d  %5d  %5d%n", destPtr, tfr, logicalBlockNo,
      //          blockOffset);

      destPtr += tfr;
      dataPtr += tfr;
      remaining -= tfr;
    }

    writeIndices ();
  }

  // ---------------------------------------------------------------------------------//
  int allocateNextBlock () throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    blocksUsed++;
    return disk.allocateNextBlock ();
  }

  // ---------------------------------------------------------------------------------//
  int getActualBlockNo (int logicalBlockNo) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    int actualBlockNo = 0;

    switch (storageType)
    {
      case TREE:
        actualBlockNo =
            masterIndexBlock.get (logicalBlockNo / 256).get (logicalBlockNo % 256);
        break;

      case SAPLING:
        if (logicalBlockNo < 256)
          actualBlockNo = indexBlock.get (logicalBlockNo);
        break;

      case SEEDLING:
        if (logicalBlockNo == 0)
          actualBlockNo = keyPointer;
        break;
    }

    if (actualBlockNo == 0)
    {
      actualBlockNo = allocateNextBlock ();
      map (logicalBlockNo, actualBlockNo);
    }

    return actualBlockNo;
  }

  // ---------------------------------------------------------------------------------//
  private void writeIndices ()
  // ---------------------------------------------------------------------------------//
  {
    if (storageType == TREE)
      masterIndexBlock.write (buffer);
    else if (storageType == SAPLING)
      indexBlock.write (buffer);
  }

  // ---------------------------------------------------------------------------------//
  void map (int logicalBlockNo, int actualBlockNo) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    if (logicalBlockNo > 255)                         // potential TREE
    {
      if (storageType != TREE)
      {
        masterIndexBlock = new MasterIndexBlock (allocateNextBlock ());

        if (storageType == SAPLING)                   // sapling -> tree
        {
          masterIndexBlock.set (0, indexBlock);
        }
        else if (storageType == SEEDLING)             // seedling -> sapling -> tree
        {
          indexBlock = new IndexBlock (allocateNextBlock ());
          indexBlock.set (0, keyPointer);
          masterIndexBlock.set (0, indexBlock);
        }

        keyPointer = masterIndexBlock.blockNo;
        storageType = TREE;
        indexBlock = null;
      }

      getIndexBlock (logicalBlockNo / 256).set (logicalBlockNo % 256, actualBlockNo);
    }
    else if (logicalBlockNo > 0)                      // potential SAPLING
    {
      if (storageType == TREE)                        // already a tree
      {
        getIndexBlock (0).set (logicalBlockNo, actualBlockNo);
      }
      else if (storageType == SAPLING)                // already a sapling
      {
        indexBlock.set (logicalBlockNo, actualBlockNo);
      }
      else                                            // new file or already a seedling
      {
        indexBlock = new IndexBlock (allocateNextBlock ());
        if (storageType == SEEDLING)                  // seedling -> sapling
          indexBlock.set (0, keyPointer);

        keyPointer = indexBlock.blockNo;
        storageType = SAPLING;
        indexBlock.set (logicalBlockNo, actualBlockNo);
      }
    }
    else if (logicalBlockNo == 0)                     // potential SEEDLING
    {
      if (storageType == TREE)                        // already a tree
      {
        getIndexBlock (0).set (0, actualBlockNo);
      }
      else if (storageType == SAPLING)                // already a sapling
      {
        indexBlock.set (0, actualBlockNo);
      }
      else
      {
        keyPointer = actualBlockNo;
        storageType = SEEDLING;
      }
    }
    else
      System.out.println ("Error");
  }

  // ---------------------------------------------------------------------------------//
  IndexBlock getIndexBlock (int position) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    IndexBlock indexBlock = masterIndexBlock.get (position);

    if (indexBlock == null)
    {
      indexBlock = new IndexBlock (allocateNextBlock ());
      masterIndexBlock.set (position, indexBlock);
    }

    return indexBlock;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (UNDERLINE);
    text.append ("File Entry\n");
    text.append (UNDERLINE);
    int blockNo = ptr / BLOCK_SIZE;
    text.append (String.format ("Block ............ %04X%n", blockNo));
    text.append (
        String.format ("Entry ............ %02X%n", ((ptr % BLOCK_SIZE) - 4) / 39 + 1));
    text.append (String.format ("Storage type ..... %02X  %s%n", storageType,
        ProdosDisk.storageTypes[storageType]));
    text.append (String.format ("Name length ...... %02X%n", fileName.length ()));
    text.append (String.format ("File name ........ %s%n", fileName));
    text.append (String.format ("File type ........ %02X%n", fileType));
    text.append (String.format ("Key pointer ...... %04X%n", keyPointer));
    text.append (String.format ("Blocks used ...... %d%n", blocksUsed));
    text.append (String.format ("EOF .............. %d%n", eof));
    text.append (String.format ("Created .......... %s%n", creationDate));
    text.append (String.format ("Version .......... %02X%n", version));
    text.append (String.format ("Min version ...... %02X%n", minVersion));
    text.append (String.format ("Access ........... %02X%n", access));
    text.append (String.format ("Aux .............. %d%n", auxType));
    text.append (String.format ("Modified ......... %s%n", modifiedDate));
    text.append (String.format ("Header ptr ....... %04X%n", headerPointer));
    text.append (UNDERLINE);

    return text.toString ();
  }
}
