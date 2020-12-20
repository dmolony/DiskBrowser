package com.bytezone.diskbrowser.gui;

import java.util.EventObject;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.HybridDisk;
import com.bytezone.diskbrowser.disk.FormattedDisk;

// -----------------------------------------------------------------------------------//
class FileSelectedEvent extends EventObject
// -----------------------------------------------------------------------------------//
{
  public final AppleFileSource appleFileSource;
  boolean redo;
  int volumeNo = -1;

  // ---------------------------------------------------------------------------------//
  FileSelectedEvent (Object source, AppleFileSource appleFileSource)
  // ---------------------------------------------------------------------------------//
  {
    super (source);
    this.appleFileSource = appleFileSource;

    // If a file is selected from a disk which is contained in a Dual-dos disk, then the DDS
    // must be told so that it can ensure its internal currentDisk is set correctly
    FormattedDisk fd = appleFileSource.getFormattedDisk ();
    HybridDisk ddd = (HybridDisk) fd.getParent ();
    if (ddd != null)
    {
      ddd.setCurrentDisk (fd);
      volumeNo = ddd.getCurrentDiskNo ();
    }
  }

  // ---------------------------------------------------------------------------------//
  public String toText ()
  // ---------------------------------------------------------------------------------//
  {
    return appleFileSource.getUniqueName ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return appleFileSource.toString ();
  }
}