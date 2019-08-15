package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

class DosVTOCSector extends AbstractSector
{
  DosDisk parentDisk;
  int volume;
  int dosVersion;       // 1, 2, 3 or 0x41, 0x42, 0x43...
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
    dosVersion = buffer[3];
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
    return dosVersion <= 3 ? createDosText () : createDos4Text ();
  }

  private String createDos4Text ()
  {
    StringBuilder text = getHeader ("DOS 4 VTOC Sector");
    addText (text, buffer, 0, 1, "Not used");
    addText (text, buffer, 1, 2, "First directory track/sector");
    addText (text, buffer, 3, 1, "DOS release number");
    addText (text, buffer, 4, 1, "Build number");
    addText (text, buffer, 5, 1, "Ram DOS " + (char) (buffer[5] & 0x7F));

    addTextAndDecimal (text, buffer, 6, 1, "Diskette volume");
    String diskType =
        buffer[7] == (byte) 0xC4 ? " = Data" : buffer[7] == (byte) 0xC2 ? " = Boot" : "";
    addText (text, buffer, 7, 1, "Volume type: " + (char) (buffer[7] & 0x7F) + diskType);

    int ptr = 8;
    addText (text, buffer, ptr, 4, "Volume name: " + getName (buffer, ptr));
    for (int j = 4; j < 24; j += 4)
      addText (text, buffer, ptr + j, 4, "");

    addText (text, buffer, 0x20, 3,
        "Date/time initialised: " + Utility.getDateTime (buffer, 0x20));
    addText (text, buffer, 0x23, 3, "");
    addText (text, buffer, 0x26, 1, "VTOC Phase");

    addTextAndDecimal (text, buffer, 0x27, 1, "Maximum TS pairs");
    addText (text, buffer, 0x28, 2, "Volume library");
    addText (text, buffer, 0x2A, 3,
        "Date/time modified: " + Utility.getDateTime (buffer, 0x2A));
    addText (text, buffer, 0x2D, 3, "");

    addTextAndDecimal (text, buffer, 0x30, 1, "Last allocated track");
    addText (text, buffer, 0x31, 1, "Direction to look when allocating the next file");
    addText (text, buffer, 0x32, 2, "Not used");
    addTextAndDecimal (text, buffer, 0x34, 1, "Maximum tracks");
    addTextAndDecimal (text, buffer, 0x35, 1, "Maximum sectors");
    addTextAndDecimal (text, buffer, 0x36, 2, "Bytes per sector");

    boolean bootSectorEmpty = parentDisk.getDisk ().isSectorEmpty (0);
    int firstSector = 0x38;
    int max = maxTracks * 4 + firstSector;
    for (int i = firstSector; i < max; i += 4)
    {
      String extra = "";
      if (i == firstSector && bootSectorEmpty)
        extra = "(unusable)";
      else if (i == 124)
        extra = "(VTOC and Catalog)";
      addText (text, buffer, i, 4, String.format ("Track %02X  %s  %s",
          (i - firstSector) / 4, getBitmap (buffer[i], buffer[i + 1]), extra));
    }

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  private String createDosText ()
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
    int firstSector = 0x38;
    int max = maxTracks * 4 + firstSector;
    for (int i = firstSector; i < max; i += 4)
    {
      String extra = "";
      if (i == firstSector && bootSectorEmpty)
        extra = "(unusable)";
      //      else if (i <= 64 && !bootSectorEmpty)
      //        extra = "(reserved for DOS)";
      //      else if (i == 124)
      //        extra = "(VTOC and Catalog)";
      addText (text, buffer, i, 4, String.format ("Track %02X  %s  %s",
          (i - firstSector) / 4, getBitmap (buffer[i], buffer[i + 1]), extra));
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
    int firstSector = 0x38;
    int max = maxTracks * 4 + firstSector;
    for (int i = firstSector; i < max; i += 4)
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

  // duplicate of DosCatalogSector.getName()
  private String getName (byte[] buffer, int offset)
  {
    StringBuilder text = new StringBuilder ();
    int max = 24;
    for (int i = 0; i < max; i++)
    {
      int c = buffer[i + offset] & 0xFF;
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

  @Override
  public String toString ()
  {
    StringBuffer text = new StringBuffer ();
    text.append ("DOS version      : 3." + dosVersion);
    text.append ("\nVolume           : " + volume);
    text.append ("\nMax TS pairs     : " + maxTSPairs);
    text.append ("\nLast allocated T : " + lastAllocTrack);
    text.append ("\nDirection        : " + direction);

    return text.toString ();
  }
}