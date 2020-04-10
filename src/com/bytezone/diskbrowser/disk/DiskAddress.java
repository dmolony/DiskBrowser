package com.bytezone.diskbrowser.disk;

// -----------------------------------------------------------------------------------//
public interface DiskAddress extends Comparable<DiskAddress>
// -----------------------------------------------------------------------------------//
{
  public int getBlockNo ();

  public int getTrackNo ();

  public int getSectorNo ();

  public Disk getDisk ();

  public boolean matches (DiskAddress other);

  // ---------------------------------------------------------------------------------//
  public default byte[] readBlock ()
  // ---------------------------------------------------------------------------------//
  {
    return getDisk ().readBlock (this);
  }

  // ---------------------------------------------------------------------------------//
  public default boolean isValidAddress ()
  // ---------------------------------------------------------------------------------//
  {
    return getDisk ().isValidAddress (this);
  }
}