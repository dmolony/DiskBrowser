package com.bytezone.diskbrowser.gui;

import java.util.EventObject;

import com.bytezone.diskbrowser.disk.DiskFactory;
import com.bytezone.diskbrowser.disk.FormattedDisk;

public class DiskSelectedEvent extends EventObject
{
  private final FormattedDisk owner;
  boolean redo;

  public DiskSelectedEvent (Object source, FormattedDisk disk)
  {
    super (source);
    this.owner = disk;
  }

  public FormattedDisk getFormattedDisk ()
  {
    return owner;
  }

  @Override
  public String toString ()
  {
    return owner.getDisk ().getFile ().getAbsolutePath ();
  }

  public String toText ()
  {
    return owner.getAbsolutePath ();
  }

  public static DiskSelectedEvent create (Object source, String path)
  {
    FormattedDisk formattedDisk = DiskFactory.createDisk (path);
    System.out.println (formattedDisk);
    return formattedDisk == null ? null : new DiskSelectedEvent (source, formattedDisk);
  }
}