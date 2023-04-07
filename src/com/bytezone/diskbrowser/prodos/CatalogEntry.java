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
  static String[] storageTypes = { "Del", "Sdl", "Sap", "Tre", "Pas", "Ext", "", "", "",
      "", "", "", "", "DIR", "SDH", "VDH" };
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
  List<DiskAddress> resourceBlocks = new ArrayList<> ();

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

    created = Utility.getAppleDate (entryBuffer, 0x18);
    version = entryBuffer[0x1C] & 0xFF;
    minVersion = entryBuffer[0x1D] & 0xFF;
    access = entryBuffer[0x1E] & 0xFF;
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
    return String.format ("%04X:%02X  %-15s  %s", blockNo, entryNo, name,
        storageTypes[storageType]);
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

    for (DiskAddress diskAddress : resourceBlocks)
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
    text.append (String.format ("Access ........ %02X%n", access));
    text.append (String.format ("Storage type... %02X%n", storageType));
    text.append (String.format ("Created ....... %s%n",
        created == null ? "" : created.format (ProdosDisk.df)));
    text.append (String.format ("Version ....... %d%n", version));

    return text.toString ();
  }

  // https://comp.sys.apple2.narkive.com/lOfvHRLD/prodos-storage-type-and-user-types
  // Two previously unused bytes in each file's directory entry are now used to 
  // indicate the case of a filename. The bytes are at relative locations 
  // +$1C and +$1D in each directory entry, and were previously labeled version 
  // and min_version. Since ProDOS 8 never actually used these bytes for version 
  // checking (except in one case, discussed below), they are now used to store 
  // lowercase information. (In the Volume header, bytes +$1A and +$1B are used instead.)
}