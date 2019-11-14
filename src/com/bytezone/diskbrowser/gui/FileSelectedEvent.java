package com.bytezone.diskbrowser.gui;

import java.util.EventObject;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.DualDosDisk;

public class FileSelectedEvent extends EventObject
{
  public final AppleFileSource appleFileSource;
  boolean redo;

  public FileSelectedEvent (Object source, AppleFileSource appleFileSource)
  {
    super (source);
    this.appleFileSource = appleFileSource;

    // If a file is selected from a disk which is contained in a Dual-dos disk, then the DDS
    // must be told so that it can ensure its internal currentDisk is set correctly
    DualDosDisk ddd = (DualDosDisk) appleFileSource.getFormattedDisk ().getParent ();
    if (ddd != null)
      ddd.setCurrentDisk (appleFileSource.getFormattedDisk ());
  }

  @Override
  public String toString ()
  {
    return appleFileSource.getUniqueName ();
  }

  public String toText ()
  {
    return appleFileSource.getUniqueName ();
  }

  public static FileSelectedEvent create (Object source, AppleFileSource afs)
  {
    return new FileSelectedEvent (source, afs);
  }
}