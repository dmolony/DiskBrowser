package com.bytezone.diskbrowser.disk;

public class AppleDiskAddress implements DiskAddress
{
  private final int block;
  private final int track;
  private final int sector;
  public final Disk owner;
  private boolean zeroFlag;

  public AppleDiskAddress (Disk owner, int block)
  {
    this.owner = owner;
    this.block = block;
    int sectorsPerTrack = owner.getSectorsPerTrack ();
    this.track = block / sectorsPerTrack;
    this.sector = block % sectorsPerTrack;
  }

  public AppleDiskAddress (Disk owner, int track, int sector)
  {
    this.owner = owner;
    zeroFlag = (track & 0x40) != 0;
    this.track = track & 0x3F;
    this.sector = sector & 0x1F;
    this.block = this.track * owner.getSectorsPerTrack () + this.sector;
  }

  public boolean zeroFlag ()
  {
    return zeroFlag;
  }

  @Override
  public int compareTo (DiskAddress that)
  {
    return this.block - that.getBlock ();
  }

  @Override
  public boolean matches (DiskAddress that)
  {
    return that != null && this.block == that.getBlock ();
  }

  @Override
  public int getBlock ()
  {
    return block;
  }

  @Override
  public int getSector ()
  {
    return sector;
  }

  @Override
  public int getTrack ()
  {
    return track;
  }

  @Override
  public Disk getDisk ()
  {
    return owner;
  }

  @Override
  public String toString ()
  {
    return String.format ("[Block=%3d, Track=%2d, Sector=%2d, Zero=%s]", block, track,
        sector, zeroFlag);
  }
}