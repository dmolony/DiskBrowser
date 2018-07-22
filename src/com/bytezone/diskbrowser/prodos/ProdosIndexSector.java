package com.bytezone.diskbrowser.prodos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class ProdosIndexSector extends AbstractSector
{
  private final String name;

  ProdosIndexSector (String name, Disk disk, byte[] buffer, DiskAddress diskAddress)
  {
    super (disk, buffer, diskAddress);
    this.name = name;
  }

  @Override
  public String createText ()
  {
    StringBuilder text = getHeader ("Prodos Index Block : " + name);

    for (int i = 0; i < 256; i++)
    {
      text.append (
          String.format ("%02X        %02X %02X", i, buffer[i], buffer[i + 256]));
      if (buffer[i] != 0 || buffer[i + 256] != 0)
      {
        int blockNo = HexFormatter.intValue (buffer[i], buffer[i + 256]);
        String valid = disk.isValidAddress (blockNo) ? "" : " *** invalid ***";
        text.append (String.format ("         %s%s%n", "block " + blockNo, valid));
      }
      else
        text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}