package com.bytezone.diskbrowser.cpm;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;

public class CPMCatalogSector extends AbstractSector
{
  private static int CATALOG_ENTRY_SIZE = 32;

  public CPMCatalogSector (Disk disk, byte[] buffer)
  {
    super (disk, buffer);
  }

  @Override
  public String createText ()
  {
    StringBuilder text = getHeader ("Catalog Sector");

    for (int i = 0; i <= 255; i += CATALOG_ENTRY_SIZE)
    {
      if (buffer[i] == (byte) 0xE5)
        break;
      addText (text, buffer, i, 1, "User number");
      addText (text, buffer, i + 1, 4, "File name : " + new String (buffer, i + 1, 8));
      addText (text, buffer, i + 9, 3, "File type : " + new String (buffer, i + 9, 3));
      addText (text, buffer, i + 12, 1, "Extent counter LO");
      addText (text, buffer, i + 13, 1, "Reserved");
      addText (text, buffer, i + 14, 1, "Extent counter HI");
      addText (text, buffer, i + 15, 1, "Record count");
      text.append ("\n");
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }
}