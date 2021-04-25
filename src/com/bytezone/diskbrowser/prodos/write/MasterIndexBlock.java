package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.BLOCK_SIZE;

// -----------------------------------------------------------------------------------//
public class MasterIndexBlock
// -----------------------------------------------------------------------------------//
{
  int blockNo;
  IndexBlock[] indexBlocks = new IndexBlock[128];
  int totalBlocks;

  // ---------------------------------------------------------------------------------//
  MasterIndexBlock (int blockNo)
  // ---------------------------------------------------------------------------------//
  {
    this.blockNo = blockNo;
  }

  // ---------------------------------------------------------------------------------//
  void set (int position, IndexBlock indexBlock)
  // ---------------------------------------------------------------------------------//
  {
    if (indexBlocks[position] == null)
      totalBlocks++;

    indexBlocks[position] = indexBlock;
  }

  // ---------------------------------------------------------------------------------//
  IndexBlock get (int position)
  // ---------------------------------------------------------------------------------//
  {
    return indexBlocks[position];
  }

  // ---------------------------------------------------------------------------------//
  void write (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = blockNo * BLOCK_SIZE;

    for (int i = 0; i < indexBlocks.length; i++)
    {
      IndexBlock indexBlock = indexBlocks[i];
      if (indexBlock == null)
      {
        buffer[ptr + i] = 0;
        buffer[ptr + i + 0x100] = 0;
      }
      else
      {
        indexBlock.write (buffer);

        int blockNo = indexBlock.blockNo;
        buffer[ptr + i] = (byte) (blockNo & 0xFF);
        buffer[ptr + i + 0x100] = (byte) ((blockNo & 0xFF00) >>> 8);
      }
    }
  }
}
