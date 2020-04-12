package com.bytezone.diskbrowser.disk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.utilities.FileFormatException;

// -----------------------------------------------------------------------------------//
public class AppleDisk2 implements Disk2
// -----------------------------------------------------------------------------------//
{
  File file;
  int totalBlocks;
  int fileOffset;
  int blockSize;
  BlockReader blockReader;

  List<AppleDisk2Address> addressList = new ArrayList<> ();
  private final byte[] diskBuffer;        // contains the disk contents in memory

  // ---------------------------------------------------------------------------------//
  public AppleDisk2 (File file, int blocks, int blockSize) throws FileFormatException
  // ---------------------------------------------------------------------------------//
  {
    this (file, blocks, blockSize, 0);
  }

  // ---------------------------------------------------------------------------------//
  public AppleDisk2 (File file, int totalBlocks, int blockSize, int fileOffset)
      throws FileFormatException
  // ---------------------------------------------------------------------------------//
  {
    this.file = file;
    this.totalBlocks = totalBlocks;
    this.fileOffset = fileOffset;

    diskBuffer = new byte[totalBlocks * blockSize];

    try (BufferedInputStream in = new BufferedInputStream (new FileInputStream (file)))
    {

      if (fileOffset > 0)
        in.skip (fileOffset);
      int bytesRead = in.read (diskBuffer);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      throw new FileFormatException (e.getMessage ());
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<AppleDisk2Address> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return addressList.iterator ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public File getFile ()
  // ---------------------------------------------------------------------------------//
  {
    return file;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int getBlockSize ()
  // ---------------------------------------------------------------------------------//
  {
    return blockSize;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isValidAddress (int blockNo)
  // ---------------------------------------------------------------------------------//
  {
    return blockNo >= 0 && blockNo < totalBlocks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int getTotalBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return totalBlocks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] getBlock (int blockNo)
  // ---------------------------------------------------------------------------------//
  {
    return blockReader.readBlock (blockNo);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] getDiskBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    return diskBuffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setBlockReader (BlockReader blockReader)
  // ---------------------------------------------------------------------------------//
  {
    this.blockReader = blockReader;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void read (byte[] buffer, int diskOffset, int length)
  // ---------------------------------------------------------------------------------//
  {
    System.arraycopy (diskBuffer, diskOffset, buffer, 0, length);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void read (byte[] buffer, int diskOffset, int bufferOffset, int length)
  // ---------------------------------------------------------------------------------//
  {
    System.arraycopy (diskBuffer, diskOffset, buffer, bufferOffset, length);
  }
}
