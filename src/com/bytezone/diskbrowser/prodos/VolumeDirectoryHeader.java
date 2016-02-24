package com.bytezone.diskbrowser.prodos;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;

/*
 * There is only one of these - it is always the first entry in the first block.
 * Every other entry will be either a SubDirectoryHeader or a FileEntry.
 */
class VolumeDirectoryHeader extends DirectoryHeader
{
  int bitMapBlock;
  int totalBlocks;
  int freeBlocks;
  int usedBlocks;
  int totalBitMapBlocks;

  public VolumeDirectoryHeader (ProdosDisk parentDisk, byte[] entryBuffer)
  {
    super (parentDisk, entryBuffer);

    bitMapBlock = HexFormatter.intValue (entryBuffer[35], entryBuffer[36]);
    totalBlocks = HexFormatter.intValue (entryBuffer[37], entryBuffer[38]);
    if (totalBlocks == 0xFFFF | totalBlocks == 0x7FFF)
      totalBlocks = (int) disk.getFile ().length () / 4096 * 8;   // ignore extra bytes
    //    totalBitMapBlocks = (totalBlocks * 8 - 1) / 4096 + 1;
    totalBitMapBlocks = (totalBlocks - 1) / 512 + 1;

    int block = 2;
    do
    {
      dataBlocks.add (disk.getDiskAddress (block));
      byte[] buffer = disk.readSector (block);
      block = HexFormatter.intValue (buffer[2], buffer[3]);
    } while (block > 0);

    // convert the Free Sector Table
    int bitMapBytes = totalBlocks / 8; // one bit per block
    byte[] buffer = new byte[bitMapBytes];
    int bitMapBlocks = (bitMapBytes - 1) / disk.getSectorsPerTrack () + 1;
    int lastBitMapBlock = bitMapBlock + bitMapBlocks - 1;
    int ptr = 0;

    for (block = bitMapBlock; block <= lastBitMapBlock; block++)
    {
      byte[] temp = disk.readSector (block);
      int bytesToCopy = buffer.length - ptr;
      if (bytesToCopy > temp.length)
        bytesToCopy = temp.length;
      System.arraycopy (temp, 0, buffer, ptr, bytesToCopy);
      ptr += bytesToCopy;
    }

    block = 0;
    //    int max = (totalBlocks - 1) / 8 + 1;   // bytes required for sector map
    // nb disk may be truncated, so use actual number of blocks
    int max = (disk.getTotalBlocks () - 1) / 8 + 1;   // bytes required for sector map

    for (int i = 0; i < max; i++)
    {
      byte b = buffer[i];
      for (int j = 0; j < 8; j++)
      {
        if ((b & 0x80) == 0x80)
        {
          freeBlocks++;
          parentDisk.setSectorFree (block++, true);
        }
        else
        {
          usedBlocks++;
          parentDisk.setSectorFree (block++, false);
        }
        b <<= 1;
      }
    }
  }

  @Override
  public DataSource getDataSource ()
  {
    List<byte[]> blockList = new ArrayList<byte[]> ();
    int block = 2;
    do
    {
      byte[] buf = disk.readSector (block);
      blockList.add (buf);
      block = HexFormatter.intValue (buf[2], buf[3]); // next block
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

  @Override
  public List<DiskAddress> getSectors ()
  {
    List<DiskAddress> sectors = new ArrayList<DiskAddress> ();
    sectors.addAll (dataBlocks);
    return sectors;
  }

  @Override
  public String toString ()
  {
    if (false)
    {
      String locked = (access == 0x01) ? "*" : " ";
      String timeC = created == null ? "" : ProdosDisk.df.format (created.getTime ());
      return String.format ("   %s%-42s %15s", locked, "/" + name, timeC);
    }
    return name;
  }
}