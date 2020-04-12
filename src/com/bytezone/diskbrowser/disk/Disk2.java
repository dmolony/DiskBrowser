package com.bytezone.diskbrowser.disk;

import java.io.File;

// -----------------------------------------------------------------------------------//
public interface Disk2 extends Iterable<AppleDisk2Address>
//-----------------------------------------------------------------------------------//
{
  public File getFile ();

  public int getBlockSize ();                   // bytes per block - 256 or 512

  public int getTotalBlocks ();                 // blocks per disk - usually 560 or 280

  public boolean isValidAddress (int blockNo);

  public byte[] getDiskBuffer ();

  public byte[] getBlock (int blockNo);

  public void setBlockReader (BlockReader blockReader);

  public void read (byte[] buffer, int diskOffset, int length);

  public void read (byte[] buffer, int diskOffset, int bufferOffset, int length);
}
