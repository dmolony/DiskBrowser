package com.bytezone.diskbrowser.prodos;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class VolumeDirectoryHeader extends DirectoryHeader
// -----------------------------------------------------------------------------------//
{
  protected final int bitMapBlock;
  protected int totalBlocks;
  protected int freeBlocks;
  protected int usedBlocks;
  protected int totalBitMapBlocks;

  // ---------------------------------------------------------------------------------//
  VolumeDirectoryHeader (ProdosDisk parentDisk, byte[] entryBuffer)
  // ---------------------------------------------------------------------------------//
  {
    super (parentDisk, entryBuffer);

    bitMapBlock = Utility.unsignedShort (entryBuffer, 35);
    totalBlocks = Utility.unsignedShort (entryBuffer, 37);

    //    if (totalBlocks == 0xFFFF || totalBlocks == 0x7FFF)
    //      totalBlocks = (int) disk.getFile ().length () / 4096 * 8;// ignore extra bytes

    totalBitMapBlocks = (totalBlocks - 1) / 512 + 1;

    int block = 2;
    do
    {
      dataBlocks.add (disk.getDiskAddress (block));
      byte[] buffer = disk.readBlock (block);
      block = Utility.unsignedShort (buffer, 2);
    } while (block > 0);

    // convert the Free Sector Table
    int bitMapBytes = (totalBlocks - 1) / 8 + 1;                  // one bit per block
    byte[] buffer = new byte[bitMapBytes];
    int bitMapBlocks = (bitMapBytes - 1) / disk.getBlocksPerTrack () + 1;
    int lastBitMapBlock = bitMapBlock + bitMapBlocks - 1;
    int ptr = 0;

    for (block = bitMapBlock; block <= lastBitMapBlock; block++)
    {
      byte[] temp = disk.readBlock (block);
      int bytesToCopy = buffer.length - ptr;
      if (bytesToCopy > temp.length)
        bytesToCopy = temp.length;
      System.arraycopy (temp, 0, buffer, ptr, bytesToCopy);
      ptr += bytesToCopy;
    }

    // nb1 dual-dos disk needs to use totalBlocks obtained from disk
    // int max1 = (totalBlocks - 1) / 8 + 1;             // bytes required for sector map
    // nb2 hard disk may be truncated, so use actual number of blocks
    // int max2 = (disk.getTotalBlocks () - 1) / 8 + 1;  // bytes required for sector map

    int max = (Math.min (totalBlocks, disk.getTotalBlocks ()) - 1) / 8 + 1;
    block = 0;

    for (int i = 0; i < max; i++)
    {
      byte b = buffer[i];
      for (int j = 0; j < 8; j++)
      {
        boolean free = (b & 0x80) != 0;
        if (free)
          freeBlocks++;
        parentDisk.setSectorFree (block++, free);
        b <<= 1;
      }
    }
    usedBlocks = totalBlocks - freeBlocks;    // totalBlocks may not be a multiple of 8
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getDataSource ()
  // ---------------------------------------------------------------------------------//
  {
    List<byte[]> blockList = new ArrayList<> ();
    int block = 2;
    do
    {
      byte[] buf = disk.readBlock (block);
      blockList.add (buf);
      block = Utility.intValue (buf[2], buf[3]); // next block
    } while (block > 0);

    byte[] fullBuffer = new byte[blockList.size () * 507];
    int offset = 0;
    for (byte[] bfr : blockList)
    {
      System.arraycopy (bfr, 4, fullBuffer, offset, 507);
      offset += 507;
    }
    return new ProdosDirectory (parentDisk, name, fullBuffer, totalBlocks, freeBlocks,
        usedBlocks);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getSectors ()
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> sectors = new ArrayList<> ();
    sectors.addAll (dataBlocks);
    return sectors;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    if (false)
    {
      String locked = (access == 0x01) ? "*" : " ";
      String timeC = created == null ? "" : created.format (ProdosDisk.df);
      return String.format ("   %s%-42s %15s", locked, "/" + name, timeC);
    }
    return name;
  }
}