package com.bytezone.diskbrowser.gui;

import java.util.EventObject;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.DualDosDisk;

public class FileSelectedEvent extends EventObject
{
  public final AppleFileSource file;
  boolean redo;

  public FileSelectedEvent (Object source, AppleFileSource file)
  {
    super (source);
    this.file = file;

    // If a file is selected from a disk which is contained in a Dual-dos disk, then the DDS
    // must be told so that it can ensure its internal currentDisk is set correctly
    DualDosDisk ddd = (DualDosDisk) file.getFormattedDisk ().getParent ();
    if (ddd != null)
      ddd.setCurrentDisk (file);
  }

  @Override
  public String toString ()
  {
    return file.getUniqueName ();
  }

  public String toText ()
  {
    return file.getUniqueName ();
  }

  public static FileSelectedEvent create (Object source, AppleFileSource afs)
  {
    return new FileSelectedEvent (source, afs);
  }
}