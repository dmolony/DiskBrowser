package com.bytezone.diskbrowser.disk;

import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// -----------------------------------------------------------------------------------//
public class SectorList extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  List<DiskAddress> sectors;
  FormattedDisk formattedDisk;

  // ---------------------------------------------------------------------------------//
  public SectorList (FormattedDisk formattedDisk, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    super ("noname", null);

    this.sectors = sectors;
    this.formattedDisk = formattedDisk;

    Disk disk = formattedDisk.getDisk ();
    int ptr = 0;
    buffer = new byte[sectors.size () * disk.getBlockSize ()];

    for (DiskAddress da : sectors)
    {
      if (!disk.isValidAddress (da))
        break;
      byte[] tempBuffer = disk.readBlock (da);
      System.arraycopy (tempBuffer, 0, buffer, ptr, disk.getBlockSize ());
      ptr += disk.getBlockSize ();
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("Block  Sector Type         Owner\n");
    text.append (
        "-----  ------------------  ---------------------------------------------\n");

    for (DiskAddress da : sectors)
    {
      SectorType sectorType = formattedDisk.getSectorType (da);
      String owner = formattedDisk.getSectorFilename (da);
      if (owner == null)
        owner = "";
      text.append (
          String.format (" %04X  %-18s  %s%n", da.getBlockNo (), sectorType.name, owner));
    }

    return text.toString ();
  }
}