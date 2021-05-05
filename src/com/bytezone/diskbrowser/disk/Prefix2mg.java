package com.bytezone.diskbrowser.disk;

import com.bytezone.diskbrowser.utilities.Utility;

// http://apple2.org.za/gswv/a2zine/Docs/DiskImage_2MG_Info.txt
// -----------------------------------------------------------------------------------//
public class Prefix2mg
// -----------------------------------------------------------------------------------//
{
  String[] creators = { "!nfc", "B2TR", "CTKG", "CdrP", "ShIm", "WOOF", "XGS!" };
  String[] images = { "Dos3.3", "Prodos", "Nibbized" };

  String prefix;
  String creator;
  int headerSize;
  int version;
  int format;
  int flags;
  int length;
  int blocks;
  int offset;
  int commentOffset;
  int commentLength;
  int creatorOffset;
  int creatorLength;

  boolean flagsLocked;
  int flagsVolume;

  // ---------------------------------------------------------------------------------//
  public Prefix2mg (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    prefix = new String (buffer, 0, 4);
    creator = new String (buffer, 4, 4);
    headerSize = Utility.getWord (buffer, 0x08);
    version = Utility.getWord (buffer, 0x0A);
    format = Utility.getLong (buffer, 0x0C);
    flags = Utility.getLong (buffer, 0x10);
    blocks = Utility.getLong (buffer, 0x14);       // 1600
    offset = Utility.getLong (buffer, 0x18);
    length = Utility.getLong (buffer, 0x1C);
    commentOffset = Utility.getLong (buffer, 0x20);
    commentLength = Utility.getLong (buffer, 0x24);
    creatorOffset = Utility.getLong (buffer, 0x28);
    creatorLength = Utility.getLong (buffer, 0x2C);

    flagsLocked = (flags & 0x80000000) != 0;
    if ((flags & 0x0100) != 0)
      flagsVolume = flags & 0xFF;
    if (format == 0 && flagsVolume == 0)
      flagsVolume = 254;

    // see /Asimov disks/images/gs/os/prodos16/ProDOS 16v1_3.2mg
    System.out.println (this);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Prefix         : %s%n", prefix));
    text.append (String.format ("Creator        : %s%n", creator));
    text.append (String.format ("Header         : %d%n", headerSize));
    text.append (String.format ("Version        : %d%n", version));
    text.append (String.format ("Format         : %02X%n", format));
    text.append (String.format ("Flags          : %,d%n", flags));
    text.append (String.format ("Locked         : %s%n", flagsLocked));
    text.append (String.format ("DOS Volume     : %,d%n", flagsVolume));
    text.append (String.format ("Blocks         : %,d%n", blocks));
    text.append (String.format ("Offset         : %,d%n", offset));
    text.append (String.format ("Length         : %08X (%<,d)%n", length));
    text.append (String.format ("Comment Offset : %,d%n", commentOffset));
    text.append (String.format ("Comment Length : %08X (%<,d)%n", commentLength));
    text.append (String.format ("Creator Offset : %,d%n", creatorOffset));
    text.append (String.format ("Creator Length : %08X (%<,d)", creatorLength));

    return text.toString ();
  }
}
