package com.bytezone.diskbrowser.disk;

// -----------------------------------------------------------------------------------//
public interface DiskAddress extends Comparable<DiskAddress>
// -----------------------------------------------------------------------------------//
{
  public int getBlock ();

  public int getTrack ();

  public int getSector ();

  public Disk getDisk ();

  public boolean matches (DiskAddress other);

  // ---------------------------------------------------------------------------------//
  public default byte[] readSector ()
  // ---------------------------------------------------------------------------------//
  {
    return getDisk ().readSector (this);
  }

  // ---------------------------------------------------------------------------------//
  public default boolean isValidAddress ()
  // ---------------------------------------------------------------------------------//
  {
    return getDisk ().isValidAddress (this);
  }
}