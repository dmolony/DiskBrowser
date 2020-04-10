package com.bytezone.diskbrowser.disk;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

// -----------------------------------------------------------------------------------//
public interface Disk extends Iterable<DiskAddress>
// -----------------------------------------------------------------------------------//
{
  public long getBootChecksum ();

  public void setEmptyByte (byte value);

  public int getTotalBlocks ();                 // blocks per disk - usually 560 or 280

  public int getTotalTracks ();                 // usually 35

  public int getBlockSize ();                   // bytes per block - 256 or 512

  public void setBlockSize (int blockSize);

  public int getTrackSize ();                   // bytes per track - 4096

  public int getBlocksPerTrack ();              // 8 or 16

  public void setInterleave (int interleave);

  public int getInterleave ();

  public DiskAddress getDiskAddress (int block);

  public List<DiskAddress> getDiskAddressList (int... blocks);

  public DiskAddress getDiskAddress (int track, int sector);

  public byte[] readBlock (int block);

  public byte[] readBlock (int track, int sector);

  public byte[] readBlock (DiskAddress da);

  public byte[] readBlocks (List<DiskAddress> daList);

  public void writeBlock (DiskAddress da, byte[] buffer);

  public boolean isBlockEmpty (int block);

  public boolean isBlockEmpty (int track, int sector);

  public boolean isBlockEmpty (DiskAddress da);

  public boolean isValidAddress (int block);

  public boolean isValidAddress (int track, int sector);

  public boolean isValidAddress (DiskAddress da);

  public File getFile ();

  public void addActionListener (ActionListener listener);

  public void removeActionListener (ActionListener listener);
}