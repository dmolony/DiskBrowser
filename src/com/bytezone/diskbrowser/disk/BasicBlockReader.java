package com.bytezone.diskbrowser.disk;

import java.util.List;

// -----------------------------------------------------------------------------------//
public class BasicBlockReader implements BlockReader
// -----------------------------------------------------------------------------------//
{
  Disk2 disk;

  // ---------------------------------------------------------------------------------//
  public BasicBlockReader (Disk2 disk)
  // ---------------------------------------------------------------------------------//
  {
    this.disk = disk;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] readBlock (int blockNo)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = new byte[disk.getBlockSize ()];
    int offset = disk.getBlockSize () * blockNo;
    System.arraycopy (disk.getDiskBuffer (), offset, buffer, offset, buffer.length);
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] readBlock (Disk2Address diskAddress)
  // ---------------------------------------------------------------------------------//
  {
    return readBlock (diskAddress.getBlockNo ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] readBlocks (List<Disk2Address> diskAddresses)
  // ---------------------------------------------------------------------------------//
  {
    int blockSize = disk.getBlockSize ();
    byte[] buffer = new byte[diskAddresses.size () * blockSize];
    int ptr = 0;
    for (Disk2Address diskAddress : diskAddresses)
    {
      disk.read (buffer, diskAddress.getBlockNo () * disk.getBlockSize (), ptr,
          blockSize);
      ptr += blockSize;
    }
    return buffer;
  }
}
