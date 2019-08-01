package com.bytezone.diskbrowser.nib;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class RawDiskSector
{
  final DiskAddressField addressField;
  byte[] buffer;

  RawDiskSector (DiskAddressField addressField)
  {
    assert false : "Not used";
    this.addressField = addressField;
  }

  void setBuffer (byte[] buffer)
  {
    this.buffer = buffer;
  }

  void dump ()
  {
    System.out.println (HexFormatter.format (buffer));
  }

  @Override
  public String toString ()
  {
    return addressField.toString ();
  }
}
