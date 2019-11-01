package com.bytezone.diskbrowser.prodos;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;

abstract class CatalogEntry implements AppleFileSource
{
  ProdosDisk parentDisk;
  DirectoryHeader parentDirectory;
  String name;
  int storageType;
  GregorianCalendar created;
  int version;
  int minVersion;
  int access;
  List<DiskAddress> dataBlocks = new ArrayList<> ();
  Disk disk;

  public CatalogEntry (ProdosDisk parentDisk, byte[] entryBuffer)
  {
    this.parentDisk = parentDisk;
    this.disk = parentDisk.getDisk ();
    name = HexFormatter.getString (entryBuffer, 1, entryBuffer[0] & 0x0F);
    storageType = (entryBuffer[0] & 0xF0) >> 4;
    created = HexFormatter.getAppleDate (entryBuffer, 24);
    version = entryBuffer[28] & 0xFF;
    minVersion = entryBuffer[29] & 0xFF;
    access = entryBuffer[30] & 0xFF;
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

  @Override
  public boolean contains (DiskAddress da)
  {
    for (DiskAddress sector : dataBlocks)
      if (sector.matches (da))
        return true;
    return false;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Name .......... %s%n", name));
    text.append (String.format ("Storage type... %02X%n", storageType));
    text.append (String.format ("Created ....... %s%n",
        created == null ? "" : parentDisk.df.format (created.getTime ())));
    text.append (String.format ("Version ....... %d%n", version));

    return text.toString ();
  }
}