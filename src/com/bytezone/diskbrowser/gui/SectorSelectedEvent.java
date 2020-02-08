package com.bytezone.diskbrowser.gui;

import java.util.EventObject;
import java.util.List;

import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.disk.SectorListConverter;

// -----------------------------------------------------------------------------------//
class SectorSelectedEvent extends EventObject
// -----------------------------------------------------------------------------------//
{
  private final List<DiskAddress> sectors;
  private final FormattedDisk owner;
  boolean redo;

  // ---------------------------------------------------------------------------------//
  SectorSelectedEvent (Object source, List<DiskAddress> sectors, FormattedDisk owner)
  // ---------------------------------------------------------------------------------//
  {
    super (source);
    this.sectors = sectors;
    // always store the parent if this disk is part of a dual-dos disk
    this.owner = owner.getParent () == null ? owner : owner.getParent ();
  }

  // ---------------------------------------------------------------------------------//
  public List<DiskAddress> getSectors ()
  // ---------------------------------------------------------------------------------//
  {
    return sectors;
  }

  // ---------------------------------------------------------------------------------//
  public FormattedDisk getFormattedDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return owner;
  }

  // ---------------------------------------------------------------------------------//
  public String toText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    SectorListConverter slc = new SectorListConverter (sectors);
    text.append (slc.sectorText);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public static SectorSelectedEvent create (Object source, FormattedDisk owner,
      String sectorsText)
  // ---------------------------------------------------------------------------------//
  {
    if (sectorsText.startsWith ("$"))
      sectorsText = sectorsText.substring (3); // only for old records

    SectorListConverter slc = new SectorListConverter (sectorsText, owner.getDisk ());
    return new SectorSelectedEvent (source, slc.sectors, owner);
  }
}