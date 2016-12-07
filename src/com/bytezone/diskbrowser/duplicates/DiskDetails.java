package com.bytezone.diskbrowser.duplicates;

import java.io.File;

import com.bytezone.common.ComputeCRC32;

public class DiskDetails
{
  private final File file;
  private long checksum = -1;
  private boolean duplicate;

  public DiskDetails (File file)
  {
    this.file = file;
    duplicate = false;
  }

  public boolean isDuplicate ()
  {
    return duplicate;
  }

  public void setDuplicate (boolean value)
  {
    duplicate = value;
  }

  public String getAbsolutePath ()
  {
    return file.getAbsolutePath ();
  }

  public long getChecksum ()
  {
    if (checksum < 0)
      checksum = ComputeCRC32.getChecksumValue (file);
    return checksum;
  }

  public boolean delete ()
  {
    //    return file.delete ();
    return false;
  }

  @Override
  public String toString ()
  {
    return String.format ("%s (%s)", file.getAbsolutePath (),
        duplicate ? "duplicate" : "OK");
  }
}