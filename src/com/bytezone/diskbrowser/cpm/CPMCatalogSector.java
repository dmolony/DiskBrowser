package com.bytezone.diskbrowser.cpm;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;

// -----------------------------------------------------------------------------------//
class CPMCatalogSector extends AbstractSector
// -----------------------------------------------------------------------------------//
{
  private static int CATALOG_ENTRY_SIZE = 32;

  // ---------------------------------------------------------------------------------//
  CPMCatalogSector (Disk disk, byte[] buffer, DiskAddress diskAddress)
  // ---------------------------------------------------------------------------------//
  {
    super (disk, buffer, diskAddress);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String createText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = getHeader ("Catalog Sector");

    for (int i = 0; i <= 255; i += CATALOG_ENTRY_SIZE)
    {
      if (buffer[i] == (byte) 0xE5 && buffer[i + 1] == (byte) 0xE5)
        break;

      boolean readOnly = (buffer[i + 9] & 0x80) != 0;
      boolean systemFile = (buffer[i + 10] & 0x80) != 0;
      String type;
      String extra;

      if (readOnly || systemFile)
      {
        byte[] typeBuffer = new byte[3];
        typeBuffer[0] = (byte) (buffer[i + 9] & 0x7F);
        typeBuffer[1] = (byte) (buffer[i + 10] & 0x7F);
        typeBuffer[2] = buffer[i + 11];
        type = new String (typeBuffer).trim ();
        extra = String.format (" (%s%s)", readOnly ? "read only" : "",
            systemFile ? "system file" : "");
      }
      else
      {
        type = new String (buffer, i + 9, 3).trim ();
        extra = "";
      }

      if (buffer[i] == (byte) 0xE5)
        addText (text, buffer, i, 1, "Deleted file?");
      else
        addText (text, buffer, i, 1, "User number");
      if (buffer[i + 1] == 0)
        addText (text, buffer, i + 1, 4, "File name : ");
      else
        addText (text, buffer, i + 1, 4, "File name : " + new String (buffer, i + 1, 8));
      addText (text, buffer, i + 5, 4, "");
      addText (text, buffer, i + 9, 3, "File type : " + type + extra);
      addText (text, buffer, i + 12, 1, "Extent counter LO");
      addText (text, buffer, i + 13, 1, "Reserved");
      addText (text, buffer, i + 14, 1, "Extent counter HI");
      addText (text, buffer, i + 15, 1, "Record count");

      for (int j = 0; j < 4; j++)
        addText (text, buffer, i + 16 + j * 4, 4, "");

      text.append ("\n");
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }
}