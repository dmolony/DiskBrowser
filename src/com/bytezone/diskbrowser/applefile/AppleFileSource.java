package com.bytezone.diskbrowser.applefile;

import java.util.List;

import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.DataSource;

// -----------------------------------------------------------------------------------//
public interface AppleFileSource
// -----------------------------------------------------------------------------------//
{
  /*
   * Returns a name that uniquely identifies this object within the disk.
   */
  // ---------------------------------------------------------------------------------//
  public String getUniqueName ();
  // ---------------------------------------------------------------------------------//

  /*
   * DataSource is implemented by AbstractSector and AbstractFile, and provides
   * routines to display the data in various formats (text, hex, assembler and
   * image).
   */
  // ---------------------------------------------------------------------------------//
  public DataSource getDataSource ();
  // ---------------------------------------------------------------------------------//

  /*
   * Returns a list of sectors used by this object.
   */
  // ---------------------------------------------------------------------------------//
  public List<DiskAddress> getSectors ();
  // ---------------------------------------------------------------------------------//

  /*
   * Returns the actual FormattedDisk that owns this object.
   */
  // ---------------------------------------------------------------------------------//
  public FormattedDisk getFormattedDisk ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  public boolean contains (DiskAddress diskAddress);
  // ---------------------------------------------------------------------------------//
}