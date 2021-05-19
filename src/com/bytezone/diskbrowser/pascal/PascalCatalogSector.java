package com.bytezone.diskbrowser.pascal;

import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class PascalCatalogSector extends AbstractSector
// -----------------------------------------------------------------------------------//
{
  private final DateFormat df = DateFormat.getDateInstance (DateFormat.SHORT);
  private static String[] fileTypes =
      { "Volume", "Bad", "Code", "Text", "Info", "Data", "Graf", "Foto", "SecureDir" };

  // ---------------------------------------------------------------------------------//
  PascalCatalogSector (Disk disk, byte[] buffer, List<DiskAddress> diskAddress)
  // ---------------------------------------------------------------------------------//
  {
    super (disk, buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String createText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = getHeader ("Pascal Catalog Sectors");

    addTextAndDecimal (text, buffer, 0, 2, "First directory block");
    addTextAndDecimal (text, buffer, 2, 2, "Last directory block");

    addText (text, buffer, 4, 2, "File type : " + fileTypes[buffer[5]]);

    String name = HexFormatter.getPascalString (buffer, 6);
    addText (text, buffer, 6, 4, "");
    addText (text, buffer, 10, 4, "Volume name : " + name);
    addTextAndDecimal (text, buffer, 14, 2, "Blocks on disk");
    addTextAndDecimal (text, buffer, 16, 2, "Files on disk");
    addTextAndDecimal (text, buffer, 18, 2, "First block of volume");

    GregorianCalendar calendar = HexFormatter.getPascalDate (buffer, 20);
    String date = calendar == null ? "--" : df.format (calendar.getTime ());
    addText (text, buffer, 20, 2, "Most recent date setting : " + date);
    addTextAndDecimal (text, buffer, 22, 4, "Reserved");

    int ptr = PascalDisk.CATALOG_ENTRY_SIZE;
    int totalFiles = Utility.getShort (buffer, 16);

    while (ptr < buffer.length && totalFiles > 0)
    {
      if (buffer[ptr + 6] == 0)
        break;
      text.append ("\n");
      addTextAndDecimal (text, buffer, ptr + 0, 2, "File's first block");
      addTextAndDecimal (text, buffer, ptr + 2, 2, "File's last block");
      int type = buffer[ptr + 4] & 0x0F;
      if (type < fileTypes.length)
        addText (text, buffer, ptr + 4, 1, "File type : " + fileTypes[type]);
      int wildcard = buffer[ptr + 4] & 0xC0;
      addText (text, buffer, ptr + 5, 1, "Wildcard : " + wildcard);
      name = HexFormatter.getPascalString (buffer, ptr + 6);
      addText (text, buffer, ptr + 6, 4, "");
      addText (text, buffer, ptr + 10, 4, "");
      addText (text, buffer, ptr + 14, 4, "");
      addText (text, buffer, ptr + 18, 4, "File name : " + name);
      addTextAndDecimal (text, buffer, ptr + 22, 2, "Bytes in file's last block");

      calendar = HexFormatter.getPascalDate (buffer, ptr + 24);
      date = calendar == null ? "--" : df.format (calendar.getTime ());
      addText (text, buffer, ptr + 24, 2, "Date : " + date);

      ptr += PascalDisk.CATALOG_ENTRY_SIZE;
      --totalFiles;                           // what if there are deleted files?
    }

    return text.toString ();
  }
}