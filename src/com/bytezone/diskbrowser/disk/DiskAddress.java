package com.bytezone.diskbrowser.disk;

public interface DiskAddress extends Comparable<DiskAddress>
{
	public int getBlock ();

	public int getTrack ();

	public int getSector ();

	public Disk getDisk ();
}