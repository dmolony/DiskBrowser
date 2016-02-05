package com.bytezone.diskbrowser.prodos;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;

abstract class CatalogEntry implements AppleFileSource
{
  FormattedDisk parentDisk;
  DirectoryHeader parentDirectory;
  String name;
  int storageType;
  GregorianCalendar created;
  int version;
  int minVersion;
  int access;
  List<DiskAddress> dataBlocks = new ArrayList<DiskAddress> ();
  Disk disk;

  public CatalogEntry (ProdosDisk parentDisk, byte[] entryBuffer)
  {
    this.parentDisk = parentDisk;
    this.disk = parentDisk.getDisk ();
    name = HexFormatter.getString (entryBuffer, 1, entryBuffer[0] & 0x0F);
    storageType = (entryBuffer[0] & 0xF0) >> 4;
    created = HexFormatter.getAppleDate (entryBuffer, 24);
    version = HexFormatter.intValue (entryBuffer[28]);
    minVersion = HexFormatter.intValue (entryBuffer[29]);
    access = HexFormatter.intValue (entryBuffer[30]);
  }

  @Override
  public String getUniqueName ()
  {
    if (parentDirectory == null)
      return name;
    return parentDirectory.getUniqueName () + "/" + name;
  }

  @Override
  public FormattedDisk getFormattedDisk ()
  {
    return parentDisk;
  }
}