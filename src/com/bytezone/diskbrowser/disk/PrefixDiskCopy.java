package com.bytezone.diskbrowser.disk;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// https://www.discferret.com/wiki/Apple_DiskCopy_4.2
// -----------------------------------------------------------------------------------//
public class PrefixDiskCopy
// -----------------------------------------------------------------------------------//
{
  private String name;
  private int dataSize;
  private int tagSize;
  private int encoding;
  private int format;
  private int id;

  // ---------------------------------------------------------------------------------//
  public PrefixDiskCopy (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int nameLength = buffer[0] * 0xFF;
    if (nameLength < 1 || nameLength > 0x3F)
      name = HexFormatter.getPascalString (buffer, 0);
    dataSize = Utility.getLongBigEndian (buffer, 0x40);
    tagSize = Utility.getLongBigEndian (buffer, 0x44);
    encoding = buffer[0x50] & 0xFF;
    format = buffer[0x51] & 0xFF;
    id = Utility.getWordBigEndian (buffer, 0x52);
  }

  // ---------------------------------------------------------------------------------//
  int getBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return dataSize / 512;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Name      : %s%n", name));
    text.append (String.format ("Data size : %08X (%<,d)%n", dataSize));
    text.append (String.format ("Tag size  : %08X (%<,d)%n", tagSize));
    text.append (String.format ("Encoding  : %02X%n", encoding));
    text.append (String.format ("Format    : %02X%n", format));
    text.append (String.format ("ID        : %04X%n", id));

    return text.toString ();
  }
}
