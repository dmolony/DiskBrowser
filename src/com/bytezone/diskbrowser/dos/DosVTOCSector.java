package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class DosVTOCSector extends AbstractSector
{
  DosDisk parentDisk;
  int volume;
  int DOSVersion;
  int maxTSPairs;
  int lastAllocTrack;
  int direction;
  int freeSectors;
  int usedSectors;
  int sectorSize;
  int maxSectors;
  int maxTracks;

  public DosVTOCSector (DosDisk parentDisk, Disk disk, byte[] buffer,
      DiskAddress diskAddress)
  {
    super (disk, buffer, diskAddress);

    this.parentDisk = parentDisk;
    DOSVersion = buffer[3];
    volume = buffer[6] & 0xFF;
    maxTSPairs = buffer[39];
    lastAllocTrack = buffer[48];
    direction = buffer[49];
    maxTracks = buffer[52] & 0xFF;
    maxSectors = buffer[53] & 0xFF;
    sectorSize = HexFormatter.intValue (buffer[54], buffer[55]);
    flagSectors ();
  }

  @Override
  public String createText ()
  {
    StringBuilder text = getHeader ("VTOC Sector");
    addText (text, buffer, 0, 1, "Not used");
    addText (text, buffer, 1, 2, "First directory track/sector");
    addText (text, buffer, 3, 1, "DOS release number");
    addText (text, buffer, 4, 2, "Not used");
    addTextAndDecimal (text, buffer, 6, 1, "Diskette volume");
    addText (text, buffer, 7, 4, "Not used");
    addText (text, buffer, 11, 4, "Not used");
    addText (text, buffer, 15, 4, "Not used");
    addText (text, buffer, 19, 4, "Not used");
    addText (text, buffer, 23, 4, "Not used");
    addText (text, buffer, 27, 4, "Not used");
    addText (text, buffer, 31, 4, "Not used");
    addText (text, buffer, 35, 4, "Not used");
    addTextAndDecimal (text, buffer, 39, 1, "Maximum TS pairs");
    addText (text, buffer, 40, 4, "Not used");
    addText (text, buffer, 44, 4, "Not used");
    addTextAndDecimal (text, buffer, 48, 1, "Last allocated track");
    addText (text, buffer, 49, 1, "Direction to look when allocating the next file");
    addText (text, buffer, 50, 2, "Not used");
    addTextAndDecimal (text, buffer, 52, 1, "Maximum tracks");
    addTextAndDecimal (text, buffer, 53, 1, "Maximum sectors");
    addTextAndDecimal (text, buffer, 54, 2, "Bytes per sector");

    boolean bootSectorEmpty = parentDisk.getDisk ().isSectorEmpty (0);
    for (int i = 56; i <= 0xC3; i += 4)
    {
      String extra = "";
      if (i == 56 && bootSectorEmpty)
        extra = "(unusable)";
      else if (i <= 64 && !bootSectorEmpty)
        extra = "(reserved for DOS)";
      else if (i == 124)
        extra = "(VTOC and Catalog)";
      addText (text, buffer, i, 4, String.format ("Track %02X  %s  %s", (i - 56) / 4,
          getBitmap (buffer[i], buffer[i + 1]), extra));
    }

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  private String getBitmap (byte left, byte right)
  {
    int base = maxSectors == 13 ? 3 : 0;
    right >>= base;
    StringBuilder text = new StringBuilder ();
    for (int i = base; i < 8; i++)
    {
      if ((right & 0x01) == 1)
        text.append (".");
      else
        text.append ("X");
      right >>= 1;
    }
    for (int i = 0; i < 8; i++)
    {
      if ((left & 0x01) == 1)
        text.append (".");
      else
        text.append ("X");
      left >>= 1;
    }
    return text.toString ();
  }

  public void flagSectors ()
  {
    int block = 0;
    int base = maxSectors == 13 ? 3 : 0;
    for (int i = 56; i <= 0xc3; i += 4)
    {
      block = check (buffer[i + 1], block, base);
      block = check (buffer[i], block, 0);
    }
  }

  private int check (byte b, int block, int base)
  {
    b >>= base;
    for (int i = base; i < 8; i++)
    {
      if ((b & 0x01) == 1)
      {
        parentDisk.setSectorFree (block, true);
        ++freeSectors;
      }
      else
      {
        parentDisk.setSectorFree (block, false);
        ++usedSectors;
      }
      block++;
      b >>= 1;
    }
    return block;
  }

  @Override
  public String toString ()
  {
    StringBuffer text = new StringBuffer ();
    text.append ("DOS version      : 3." + DOSVersion);
    text.append ("\nVolume           : " + volume);
    text.append ("\nMax TS pairs     : " + maxTSPairs);
    text.append ("\nLast allocated T : " + lastAllocTrack);
    text.append ("\nDirection        : " + direction);

    return text.toString ();
  }
}