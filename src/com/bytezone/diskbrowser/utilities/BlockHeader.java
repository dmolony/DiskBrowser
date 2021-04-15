package com.bytezone.diskbrowser.utilities;

// -----------------------------------------------------------------------------------//
public class BlockHeader
// -----------------------------------------------------------------------------------//
{
  private static final byte[] NuFX = { 0x4E, (byte) 0xF5, 0x46, (byte) 0xD8 };
  private static String[] fileSystems =
      { "", "ProDOS/SOS", "DOS 3.3", "DOS 3.2", "Apple II Pascal", "Macintosh HFS",
        "Macintosh MFS", "Lisa File System", "Apple CP/M", "", "MS-DOS", "High Sierra",
        "ISO 9660", "AppleShare" };

  int headerCRC;
  int attribCount;
  int version;
  int totalThreads;
  int fileSystemID;
  int fileSystemInfo;
  byte fileSystemSeparator;
  int accessFlags;
  int fileType;
  int extraType;
  int storageType;
  int fileSystemBlockSize;
  DateTime created;
  DateTime modified;
  DateTime archived;
  int optionSize;
  int fileNameLength;
  String fileName;

  // ---------------------------------------------------------------------------------//
  public BlockHeader (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    assert Utility.isMagic (buffer, ptr, NuFX);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Header CRC ..... %,d  (%<04X)%n", headerCRC));
    text.append (String.format ("Attributes ..... %d%n", attribCount));
    text.append (String.format ("Version ........ %d%n", version));
    text.append (String.format ("Threads ........ %d%n", totalThreads));
    text.append (String.format ("File sys id .... %d (%s)%n", fileSystemID,
        fileSystems[fileSystemID]));
    text.append (String.format ("Separator ...... %s%n", fileSystemSeparator));
    text.append (String.format ("Access ......... %,d%n", accessFlags));
    text.append (String.format ("File type ...... %,d%n", fileType));
    text.append (String.format ("Aux type ....... %,d%n", extraType));
    text.append (String.format ("Stor type ...... %,d%n", storageType));
    text.append (String.format ("Created ........ %s%n", created.format ()));
    text.append (String.format ("Modified ....... %s%n", modified.format ()));
    text.append (String.format ("Archived ....... %s%n", archived.format ()));
    text.append (String.format ("Option size .... %,d%n", optionSize));
    text.append (String.format ("Filename len ... %,d%n", fileNameLength));
    text.append (String.format ("Filename ....... %s", fileName));

    return text.toString ();
  }
}
