package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

class DosCatalogSector extends AbstractSector
{
  private static final String[] fileTypes =
      { "Text file", "Integer Basic program", "Applesoft Basic program", "Binary file",
        "SS file", "Relocatable file", "AA file", "BB file" };
  private static int CATALOG_ENTRY_SIZE = 35;

  private final DosDisk dosDisk;

  public DosCatalogSector (DosDisk dosDisk, Disk disk, byte[] buffer,
      DiskAddress diskAddress)
  {
    super (disk, buffer, diskAddress);
    this.dosDisk = dosDisk;
  }

  @Override
  public String createText ()
  {
    StringBuilder text =
        getHeader ("DOS " + dosDisk.getVersionText () + " Catalog Sector");
    addText (text, buffer, 0, 1, "Not used");
    addText (text, buffer, 1, 2, "Next catalog track/sector");
    addText (text, buffer, 3, 4, "Not used");
    addText (text, buffer, 7, 4, "Not used");

    boolean dos4 = dosDisk.getVersion () > 3;

    for (int i = 11; i <= 255; i += CATALOG_ENTRY_SIZE)
    {
      text.append ("\n");
      if (dos4)
        createDos4Text (text, i);
      else
        createDos3Text (text, i);
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  private void createDos4Text (StringBuilder text, int i)
  {
    int track = buffer[i] & 0x3F;
    int sector = buffer[i + 1] & 0x1F;
    int fileType = buffer[i + 2] & 0x7F;

    boolean deleteFlag = (buffer[i] & 0x80) != 0;
    boolean zeroTrackFlag = (buffer[i] & 0x40) != 0;
    boolean lockedFlag = (buffer[i + 2] & 0x80) != 0;

    if (buffer[i] == 0 && !zeroTrackFlag)
    {
      addText (text, buffer, i + 0, 2, "");
      addText (text, buffer, i + 2, 1, "");
      addText (text, buffer, i + 3, 4, "");
      addText (text, buffer, i + 33, 2, "");
    }
    else
    {
      addText (text, buffer, i, 1,
          String.format ("TS list track (%s, %s)", deleteFlag ? "deleted" : "not deleted",
              zeroTrackFlag ? "track zero" : "not track zero"));
      addText (text, buffer, i + 1, 1, "TS list sector");
      addText (text, buffer, i + 2, 1,
          String.format ("File type (%s)", lockedFlag ? "locked" : "unlocked"));
      addText (text, buffer, i + 3, 4, getName (buffer, i + 3, 24));
      for (int j = 0; j < 20; j += 4)
        addText (text, buffer, i + 7 + j, 4, "");
      addText (text, buffer, i + 27, 3,
          "Date/time initialised: " + Utility.getDateTime (buffer, i + 27));
      addText (text, buffer, i + 30, 3, "");
      addTextAndDecimal (text, buffer, i + 33, 2, "File size");
    }
  }

  private void createDos3Text (StringBuilder text, int i)
  {
    if (buffer[i] == (byte) 0xFF)         // file is deleted
    {
      addText (text, buffer, i, 2, "DEL: file @ " + HexFormatter.format2 (buffer[i + 32])
          + " " + HexFormatter.format2 (buffer[i + 1]));
      addText (text, buffer, i + 2, 1, "DEL: File type " + getType (buffer[i + 2]));
      if (buffer[i + 3] == 0)
        addText (text, buffer, i + 3, 4, "");
      else
        addText (text, buffer, i + 3, 4, "DEL: " + getName (buffer, i + 3, 29));
      addTextAndDecimal (text, buffer, i + 33, 2, "DEL: Sector count");
    }
    else if (buffer[i] > 0)               // file exists
    {
      addText (text, buffer, i, 2, "TS list track/sector");
      addText (text, buffer, i + 2, 1, "File type " + getType (buffer[i + 2]));
      if (buffer[i + 3] == 0)
        addText (text, buffer, i + 3, 4, "");
      else
      {
        addText (text, buffer, i + 3, 4, getName (buffer, i + 3, 30));
        for (int j = 0; j < 24; j += 4)
          addText (text, buffer, i + j + 7, 4, "");
        addText (text, buffer, i + 31, 2, "");
      }
      addTextAndDecimal (text, buffer, i + 33, 2, "Sector count");
    }
    else                                  // no file
    {
      addText (text, buffer, i + 0, 2, "");
      addText (text, buffer, i + 2, 1, "");
      addText (text, buffer, i + 3, 4, "");
      addText (text, buffer, i + 33, 2, "");
    }
  }

  private String getName (byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();
    //    int max = buffer[offset] == (byte) 0xFF ? 32 : 33;
    for (int i = offset; i < offset + length; i++)
    {
      int c = buffer[i] & 0xFF;
      if (c == 136)
      {
        if (text.length () > 0)
          text.deleteCharAt (text.length () - 1);
        continue;
      }
      if (c > 127)
        c -= c < 160 ? 64 : 128;
      if (c < 32)                                 // non-printable
        text.append ("^" + (char) (c + 64));
      else
        text.append ((char) c);                   // standard ascii
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