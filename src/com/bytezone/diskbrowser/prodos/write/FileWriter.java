package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.BLOCK_SIZE;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.SAPLING;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.SEEDLING;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.TREE;

// Assumptions:
// - file does not already exist
// - disk has no interleave
// -----------------------------------------------------------------------------------//
public class FileWriter
// -----------------------------------------------------------------------------------//
{
  private final ProdosDisk disk;

  private IndexBlock indexBlock = null;
  private MasterIndexBlock masterIndexBlock = null;

  byte storageType;
  int keyPointer;
  int blocksUsed;
  int eof;

  // ---------------------------------------------------------------------------------//
  FileWriter (ProdosDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    this.disk = disk;
  }

  // ---------------------------------------------------------------------------------//
  void writeFile (byte[] dataBuffer, int eof) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    this.eof = Math.min (eof, dataBuffer.length);

    int dataPtr = 0;
    int remaining = this.eof;

    while (dataPtr < this.eof)
    {
      int actualBlockNo = register (dataPtr / BLOCK_SIZE);

      int bufferPtr = actualBlockNo * BLOCK_SIZE;
      int transfer = Math.min (remaining, BLOCK_SIZE);

      System.arraycopy (dataBuffer, dataPtr, disk.getBuffer (), bufferPtr, transfer);

      dataPtr += transfer;
      remaining -= transfer;
    }

    writeIndices ();
  }

  // ---------------------------------------------------------------------------------//
  void writeRecord (int recordNo, byte[] dataBuffer, int recordLength) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    assert recordLength > 0;

    int destPtr = recordLength * recordNo;
    int remaining = Math.min (recordLength, dataBuffer.length);
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

      System.arraycopy (dataBuffer, dataPtr, disk.getBuffer (), bufferPtr, tfr);

      destPtr += tfr;
      dataPtr += tfr;
      remaining -= tfr;
    }

    writeIndices ();
  }

  // ---------------------------------------------------------------------------------//
  private int allocateNextBlock () throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    blocksUsed++;
    return disk.allocateNextBlock ();
  }

  // ---------------------------------------------------------------------------------//
  private int getActualBlockNo (int logicalBlockNo) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    int actualBlockNo = 0;

    switch (storageType)
    {
      case TREE:
        actualBlockNo =
            masterIndexBlock.get (logicalBlockNo / 0x100).getPosition (logicalBlockNo % 0x100);
        break;

      case SAPLING:
        if (logicalBlockNo < 0x100)
          actualBlockNo = indexBlock.getPosition (logicalBlockNo);
        break;

      case SEEDLING:
        if (logicalBlockNo == 0)
          actualBlockNo = keyPointer;
        break;
    }

    if (actualBlockNo == 0)
      actualBlockNo = register (logicalBlockNo);

    return actualBlockNo;
  }

  // ---------------------------------------------------------------------------------//
  private void writeIndices ()
  // ---------------------------------------------------------------------------------//
  {
    if (storageType == TREE)
      masterIndexBlock.write (disk.getBuffer ());
    else if (storageType == SAPLING)
      indexBlock.write (disk.getBuffer ());
  }

  // ---------------------------------------------------------------------------------//
  private int register (int logicalBlockNo) throws DiskFullException
  // ---------------------------------------------------------------------------------//
  {
    int nextBlockNo = allocateNextBlock ();

    if (logicalBlockNo >= 0x100)                       // potential TREE
    {
      if (storageType != TREE)
      {
        masterIndexBlock = new MasterIndexBlock (nextBlockNo);
        nextBlockNo = allocateNextBlock ();

        if (storageType == SAPLING)                   // sapling -> tree
        {
          masterIndexBlock.set (0, indexBlock);
        }
        else if (storageType == SEEDLING)             // seedling -> sapling -> tree
        {
          indexBlock = new IndexBlock (nextBlockNo);
          nextBlockNo = allocateNextBlock ();

          indexBlock.setPosition (0, keyPointer);
          masterIndexBlock.set (0, indexBlock);
        }

        keyPointer = masterIndexBlock.blockNo;
        storageType = TREE;
        indexBlock = null;
      }

      getIndexBlock (logicalBlockNo / 0x100).setPosition (logicalBlockNo % 0x100, nextBlockNo);
    }
    else if (logicalBlockNo > 0)                      // potential SAPLING
    {
      if (storageType == TREE)                        // already a tree
      {
        getIndexBlock (0).setPosition (logicalBlockNo, nextBlockNo);
      }
      else if (storageType == SAPLING)                // already a sapling
      {
        indexBlock.setPosition (logicalBlockNo, nextBlockNo);
      }
      else                                            // new file or already a seedling
      {
        indexBlock = new IndexBlock (nextBlockNo);
        nextBlockNo = allocateNextBlock ();

        if (storageType == SEEDLING)                  // seedling -> sapling
          indexBlock.setPosition (0, keyPointer);

        keyPointer = indexBlock.blockNo;
        storageType = SAPLING;
        indexBlock.setPosition (logicalBlockNo, nextBlockNo);
      }
    }
    else if (logicalBlockNo == 0)                     // potential SEEDLING
    {
      if (storageType == TREE)                        // already a tree
      {
        getIndexBlock (0).setPosition (0, nextBlockNo);
      }
      else if (storageType == SAPLING)                // already a sapling
      {
        indexBlock.setPosition (0, nextBlockNo);
      }
      else
      {
        keyPointer = nextBlockNo;
        storageType = SEEDLING;
      }
    }
    else
      System.out.println ("Error: " + logicalBlockNo);

    return nextBlockNo;
  }

  // ---------------------------------------------------------------------------------//
  private IndexBlock getIndexBlock (int position) throws DiskFullException
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
}
