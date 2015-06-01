package com.bytezone.diskbrowser.disk;

public class AppleDiskAddress implements DiskAddress
{
  private final int block;
  private final int track;
  private final int sector;
  public final Disk owner;

  public AppleDiskAddress (int block, Disk owner)
  {
    this.owner = owner;
    this.block = block;
    int sectorsPerTrack = owner.getSectorsPerTrack ();
    this.track = block / sectorsPerTrack;
    this.sector = block % sectorsPerTrack;
  }

  public AppleDiskAddress (int track, int sector, Disk owner)
  {
    this.owner = owner;
    this.track = track;
    this.sector = sector;
    this.block = track * owner.getSectorsPerTrack () + sector;
  }

  @Override
  public String toString ()
  {
    return String.format ("[Block=%3d, Track=%2d, Sector=%2d]", block, track, sector);
  }

  public int compareTo (DiskAddress that)
  {
    return this.block - that.getBlock ();
  }

  public int getBlock ()
  {
    return block;
  }

  public int getSector ()
  {
    return sector;
  }

  public int getTrack ()
  {
    return track;
  }

  public Disk getDisk ()
  {
    return owner;
  }

  @Override
  public boolean equals (Object other)
  {
    if (other == null || getClass () != other.getClass ())
      return false;
    return this.block == ((AppleDiskAddress) other).block;
  }
}