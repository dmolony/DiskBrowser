package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class DosTSListSector extends AbstractSector
{
  String name;

  public DosTSListSector (String name, Disk disk, byte[] buffer, DiskAddress diskAddress)
  {
    super (disk, buffer, diskAddress);
    this.name = name;
  }

  public boolean isValid (DosDisk dosDisk)
  {
    System.out.println ("Validating TS List sector");

    // what is the count of blocks? does it match? this sector can't tell, there
    // might be more than one TS list

    // validate the sector, throw an exception if invalid
    for (int i = 12; i < buffer.length; i += 2)
    {
      DiskAddress da = getValidAddress (buffer, i);
      if (da == null)
      {
        System.out.println ("Invalid sector address : null");
        break;            // throw exception?
      }

      if (da.getBlock () > 0 && dosDisk.stillAvailable (da))
      {
        System.out.println ("Invalid sector address : " + da);
        break;            // throw exception?
      }
    }
    return true;
  }

  // this is in too many places
  protected DiskAddress getValidAddress (byte[] buffer, int offset)
  {
    if (disk.isValidAddress (buffer[offset], buffer[offset + 1]))
      return disk.getDiskAddress (buffer[offset], buffer[offset + 1]);
    return null;
  }

  @Override
  public String createText ()
  {
    DiskAddress da = disk.getDiskAddress (buffer[1], buffer[2]);
    String msg = da.compareTo (diskAddress) == 0 ? " (circular reference)" : "";

    StringBuilder text = getHeader ("TS List Sector : " + name);
    addText (text, buffer, 0, 1, "Not used");
    addText (text, buffer, 1, 2, "Next TS list track/sector" + msg);
    addText (text, buffer, 3, 2, "Not used");
    addTextAndDecimal (text, buffer, 5, 2, "Sector base number");
    addText (text, buffer, 7, 4, "Not used");
    addText (text, buffer, 11, 1, "Not used");

    String message;
    int sectorBase = HexFormatter.intValue (buffer[5], buffer[6]);

    for (int i = 12; i <= 255; i += 2)
    {
      if (buffer[i] == 0 && buffer[i + 1] == 0)
        message = "";
      else
        message = "Track/sector of file sector " + ((i - 10) / 2 + sectorBase);
      addText (text, buffer, i, 2, message);
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }
}