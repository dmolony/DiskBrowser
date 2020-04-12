package com.bytezone.diskbrowser.disk;

// -----------------------------------------------------------------------------------//
public class AppleDisk2Address implements Disk2Address
//-----------------------------------------------------------------------------------//
{
  private Disk2 disk;
  private int blockNo;
  private byte[] buffer;

  // ---------------------------------------------------------------------------------//
  public AppleDisk2Address (Disk2 disk, int blockNo) throws Exception
  // ---------------------------------------------------------------------------------//
  {
    this.disk = disk;
    this.blockNo = blockNo;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int getBlockNo ()
  // ---------------------------------------------------------------------------------//
  {
    return blockNo;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] getBlock ()
  // ---------------------------------------------------------------------------------//
  {
    if (buffer == null)
      buffer = disk.getBlock (blockNo);

    return buffer;
  }
}
