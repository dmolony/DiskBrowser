package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class DosTSListSector extends AbstractSector
// -----------------------------------------------------------------------------------//
{
  private final String name;

  // ---------------------------------------------------------------------------------//
  DosTSListSector (String name, Disk disk, byte[] buffer, DiskAddress diskAddress)
  // ---------------------------------------------------------------------------------//
  {
    super (disk, buffer, diskAddress);
    this.name = name;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isValid (DosDisk dosDisk)
  // ---------------------------------------------------------------------------------//
  {
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

      if (da.getBlockNo () > 0 && dosDisk.stillAvailable (da))
      {
        System.out.println ("Invalid sector address : " + da);
        break;            // throw exception?
      }
    }
    return true;
  }

  // this is in too many places
  // ---------------------------------------------------------------------------------//
  protected DiskAddress getValidAddress (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    if (disk.isValidAddress (buffer[offset], buffer[offset + 1]))
      return disk.getDiskAddress (buffer[offset], buffer[offset + 1]);
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String createText ()
  // ---------------------------------------------------------------------------------//
  {
    DiskAddress da = disk.getDiskAddress (buffer[1], buffer[2]);
    if (da == null)
      return String.format ("Invalid address: %02X %02X", buffer[1], buffer[2]);

    String msg = da.matches (diskAddress) ? " (circular reference)" : "";

    StringBuilder text = getHeader ("TS List Sector : " + name);
    addText (text, buffer, 0, 1, "Not used");
    addText (text, buffer, 1, 2, "Next TS list track/sector" + msg);

    if ((buffer[3] != 0 || buffer[4] != 0)         // not supposed to be used
        // Diags2E.dsk stores its own sector address here
        && (diskAddress.getTrackNo () == (buffer[3] & 0xFF)
            && diskAddress.getSectorNo () == (buffer[4] & 0xFF)))
      addText (text, buffer, 3, 2, "Self-reference");
    else
      addText (text, buffer, 3, 2, "Not used");

    addTextAndDecimal (text, buffer, 5, 2, "Sector base number");
    addText (text, buffer, 7, 4, "Not used");
    addText (text, buffer, 11, 1, "Not used");

    int sectorBase = Utility.unsignedShort (buffer, 5);

    for (int i = 12; i <= 255; i += 2)
    {
      if (buffer[i] == 0 && buffer[i + 1] == 0)
        msg = "";
      else
      {
        String msg2 = buffer[i] == 0x40 ? "  - track zero" : "";
        msg = String.format ("Track/sector of file sector %04X (%<,d)%s",
            ((i - 12) / 2 + sectorBase), msg2);
      }
      addText (text, buffer, i, 2, msg);
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }
}