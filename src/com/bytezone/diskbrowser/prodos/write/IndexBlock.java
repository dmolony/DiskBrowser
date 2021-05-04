package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.BLOCK_SIZE;

// -----------------------------------------------------------------------------------//
public class IndexBlock
// -----------------------------------------------------------------------------------//
{
  int blockNo;
  int[] blocks = new int[BLOCK_SIZE / 2];
  int totalBlocks;

  // ---------------------------------------------------------------------------------//
  public IndexBlock (int dataBlockNo)
  // ---------------------------------------------------------------------------------//
  {
    this.blockNo = dataBlockNo;
  }

  // ---------------------------------------------------------------------------------//
  void setPosition (int position, int actualBlockNo)
  // ---------------------------------------------------------------------------------//
  {
    if (blocks[position] == 0)
      totalBlocks++;

    blocks[position] = actualBlockNo;
  }

  // ---------------------------------------------------------------------------------//
  int get (int position)
  // ---------------------------------------------------------------------------------//
  {
    return blocks[position];
  }

  // ---------------------------------------------------------------------------------//
  void write (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = blockNo * BLOCK_SIZE;

    for (int i = 0; i < blocks.length; i++)
    {
      if (blocks[i] == 0)
      {
        buffer[ptr + i] = 0;
        buffer[ptr + i + 0x100] = 0;
      }
      else
      {
        int blockNo = blocks[i];
        buffer[ptr + i] = (byte) (blockNo & 0xFF);
        buffer[ptr + i + 0x100] = (byte) ((blockNo & 0xFF00) >>> 8);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("Index Block: %04X contains %d entries%n", blockNo,
        totalBlocks);
  }
}
