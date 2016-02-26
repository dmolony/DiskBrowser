package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class DosCatalogSector extends AbstractSector
{
  private static final String[] fileTypes =
      { "Text file", "Integer Basic program", "Applesoft Basic program", "Binary file",
        "SS file", "Relocatable file", "AA file", "BB file" };
  private static int CATALOG_ENTRY_SIZE = 35;

  public DosCatalogSector (Disk disk, byte[] buffer)
  {
    super (disk, buffer);
  }

  @Override
  public String createText ()
  {
    StringBuilder text = getHeader ("Catalog Sector");
    addText (text, buffer, 0, 1, "Not used");
    addText (text, buffer, 1, 2, "Next catalog track/sector");
    addText (text, buffer, 3, 4, "Not used");
    addText (text, buffer, 7, 4, "Not used");

    for (int i = 11; i <= 255; i += CATALOG_ENTRY_SIZE)
    {
      text.append ("\n");
      if (true)
      {
        if (buffer[i] == (byte) 0xFF)
        {
          addText (text, buffer, i + 0, 2,
                   "DEL: file @ " + HexFormatter.format2 (buffer[i + 32]) + " "
                       + HexFormatter.format2 (buffer[i + 1]));
          addText (text, buffer, i + 2, 1, "DEL: File type " + getType (buffer[i + 2]));
          if (buffer[i + 3] == 0)
            addText (text, buffer, i + 3, 4, "");
          else
            addText (text, buffer, i + 3, 4, "DEL: " + getName (buffer, i));
          addTextAndDecimal (text, buffer, i + 33, 2, "DEL: Sector count");
        }
        else if (buffer[i] > 0)
        {
          addText (text, buffer, i + 0, 2, "TS list track/sector");
          addText (text, buffer, i + 2, 1, "File type " + getType (buffer[i + 2]));
          if (buffer[i + 3] == 0)
            addText (text, buffer, i + 3, 4, "");
          else
            addText (text, buffer, i + 3, 4, getName (buffer, i));
          addTextAndDecimal (text, buffer, i + 33, 2, "Sector count");
        }
        else
        {
          addText (text, buffer, i + 0, 2, "");
          addText (text, buffer, i + 2, 1, "");
          addText (text, buffer, i + 3, 4, "");
          addText (text, buffer, i + 33, 2, "");
        }
      }
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  private String getName (byte[] buffer, int offset)
  {
    StringBuilder text = new StringBuilder ();
    int max = buffer[offset] == (byte) 0xFF ? 32 : 33;
    for (int i = 3; i < max; i++)
    {
      int c = HexFormatter.intValue (buffer[i + offset]);
      if (c == 136)
      {
        if (text.length () > 0)
          text.deleteCharAt (text.length () - 1);
        continue;
      }
      if (c > 127)
        c -= c < 160 ? 64 : 128;
      if (c < 32) // non-printable
        text.append ("^" + (char) (c + 64));
      else
        text.append ((char) c); // standard ascii
    }
    return text.toString ();
  }

  private String getType (byte value)
  {
    int type = value & 0x7F;
    boolean locked = (value & 0x80) > 0;
    int val = 7;
    for (int i = 64; i > type; val--, i /= 2)
      ;
    return "(" + fileTypes[val] + (locked ? ", locked)" : ", unlocked)");
  }
}