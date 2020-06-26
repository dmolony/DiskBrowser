package com.bytezone.diskbrowser.disk;

import com.bytezone.diskbrowser.utilities.Utility;

// http://apple2.org.za/gswv/a2zine/Docs/DiskImage_2MG_Info.txt
// -----------------------------------------------------------------------------------//
public class Prefix2mg
// -----------------------------------------------------------------------------------//
{
  String prefix;
  String creator;
  int headerSize;
  int version;
  byte format;
  int diskData;
  int blocks;

  // ---------------------------------------------------------------------------------//
  public Prefix2mg (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    prefix = new String (buffer, 0, 4);
    creator = new String (buffer, 4, 4);
    headerSize = Utility.getWord (buffer, 8);
    version = Utility.getWord (buffer, 10);
    format = buffer[12];

    diskData = Utility.getLong (buffer, 28);
    blocks = Utility.intValue (buffer[20], buffer[21]);       // 1600

    // see /Asimov disks/images/gs/os/prodos16/ProDOS 16v1_3.2mg
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Prefix    : %s%n", prefix));
    text.append (String.format ("Creator   : %s%n", creator));
    text.append (String.format ("Header    : %d%n", headerSize));
    text.append (String.format ("Version   : %d%n", version));
    text.append (String.format ("Format    : %02X%n", format));

    text.append (String.format ("Data size : %08X (%<,d)%n", diskData));
    text.append (String.format ("Blocks    : %,d%n", blocks));

    return text.toString ();
  }
}
