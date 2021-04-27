package com.bytezone.diskbrowser.prodos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
abstract class CatalogEntry implements AppleFileSource
// -----------------------------------------------------------------------------------//
{
  Disk disk;
  ProdosDisk parentDisk;

  int blockNo;
  int entryNo;

  String name;
  int storageType;

  LocalDateTime created;
  int version;
  int minVersion;
  int access;

  List<DiskAddress> dataBlocks = new ArrayList<> ();
  DirectoryHeader parentDirectory;

  // ---------------------------------------------------------------------------------//
  CatalogEntry (ProdosDisk parentDisk, byte[] entryBuffer, int blockNo, int entryNo)
  // ---------------------------------------------------------------------------------//
  {
    this.parentDisk = parentDisk;
    this.disk = parentDisk.getDisk ();
    this.blockNo = blockNo;
    this.entryNo = entryNo;

    name = HexFormatter.getString (entryBuffer, 1, entryBuffer[0] & 0x0F);
    storageType = (entryBuffer[0] & 0xF0) >> 4;

    created = Utility.getAppleDate (entryBuffer, 24);
    version = entryBuffer[28] & 0xFF;
    minVersion = entryBuffer[29] & 0xFF;
    access = entryBuffer[30] & 0xFF;
  }

  // ---------------------------------------------------------------------------------//
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%04X:%02X  %-15s  %02X", blockNo, entryNo, name, storageType);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getUniqueName ()
  // ---------------------------------------------------------------------------------//
  {
    if (parentDirectory == null)
      return name;
    return parentDirectory.getUniqueName () + "/" + name;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public FormattedDisk getFormattedDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return parentDisk;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean contains (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    for (DiskAddress diskAddress : dataBlocks)
      if (diskAddress.matches (da))
        return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Name .......... %s%n", name));
    text.append (String.format ("Storage type... %02X%n", storageType));
    text.append (String.format ("Created ....... %s%n",
        created == null ? "" : created.format (ProdosDisk.df)));
    text.append (String.format ("Version ....... %d%n", version));

    return text.toString ();
  }
}