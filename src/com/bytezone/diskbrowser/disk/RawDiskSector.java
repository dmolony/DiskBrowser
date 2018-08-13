package com.bytezone.diskbrowser.disk;

public class RawDiskSector
{
  final DiskAddressField addressField;
  byte[] buffer;

  RawDiskSector (DiskAddressField addressField)
  {
    this.addressField = addressField;
  }

  void setBuffer (byte[] buffer)
  {
    this.buffer = buffer;
  }

  @Override
  public String toString ()
  {
    return addressField.toString ();
  }
}
