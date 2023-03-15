package com.bytezone.diskbrowser.nufx;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.fileTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.DateTime;
import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class Record
// -----------------------------------------------------------------------------------//
{
  private static final byte[] NuFX = { 0x4E, (byte) 0xF5, 0x46, (byte) 0xD8 };
  private static String[] fileSystems = { "", "ProDOS/SOS", "DOS 3.3", "DOS 3.2",
      "Apple II Pascal", "Macintosh HFS", "Macintosh MFS", "Lisa File System",
      "Apple CP/M", "", "MS-DOS", "High Sierra", "ISO 9660", "AppleShare" };

  private static String[] storage = { "", "Seedling", "Sapling", "Tree", "", "Extended",
      "", "", "", "", "", "", "", "Subdirectory" };

  private static String[] accessChars = { "D", "R", "B", "", "", "I", "W", "R" };
  private static String threadFormats[] = { "unc", "sq ", "lz1", "lz2", "", "" };

  private final int totThreads;
  private final int crc;
  private final char separator;
  private final int fileSystemID;
  private final int attributes;
  private final int version;
  private final int access;
  private final int fileType;
  private final int auxType;
  private final int storType;
  private final DateTime created;
  private final DateTime modified;
  private final DateTime archived;
  private final int optionSize;
  private final int fileNameLength;
  private final String fileName;

  final List<Thread> threads = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public Record (byte[] buffer, int dataPtr) throws FileFormatException
  // ---------------------------------------------------------------------------------//
  {
    // check for NuFX
    if (!Utility.isMagic (buffer, dataPtr, NuFX))
      throw new FileFormatException ("NuFX not found");

    crc = Utility.getShort (buffer, dataPtr + 4);
    attributes = Utility.getShort (buffer, dataPtr + 6);
    version = Utility.getShort (buffer, dataPtr + 8);
    totThreads = Utility.getLong (buffer, dataPtr + 10);
    fileSystemID = Utility.getShort (buffer, dataPtr + 14);
    separator = (char) (buffer[dataPtr + 16] & 0x00FF);
    access = Utility.getLong (buffer, dataPtr + 18);
    fileType = Utility.getLong (buffer, dataPtr + 22);
    auxType = Utility.getLong (buffer, dataPtr + 26);
    storType = Utility.getShort (buffer, dataPtr + 30);
    created = new DateTime (buffer, dataPtr + 32);
    modified = new DateTime (buffer, dataPtr + 40);
    archived = new DateTime (buffer, dataPtr + 48);
    optionSize = Utility.getShort (buffer, dataPtr + 56);
    fileNameLength = Utility.getShort (buffer, dataPtr + attributes - 2);

    int len = attributes + fileNameLength - 6;
    byte[] crcBuffer = new byte[len + totThreads * 16];
    System.arraycopy (buffer, dataPtr + 6, crcBuffer, 0, crcBuffer.length);

    if (crc != Utility.getCRC (crcBuffer, crcBuffer.length, 0))
    {
      System.out.println ("***** Record CRC mismatch *****");
      throw new FileFormatException ("Record CRC failed");
    }

    if (fileNameLength > 0)
    {
      int start = dataPtr + attributes;
      int end = start + fileNameLength;
      for (int i = start; i < end; i++)
        buffer[i] &= 0x7F;
      fileName = new String (buffer, start, fileNameLength);
    }
    else
      fileName = "";
  }

  // ---------------------------------------------------------------------------------//
  boolean isValidFileSystem ()
  // ---------------------------------------------------------------------------------//
  {
    return fileSystemID <= 4 || fileSystemID == 8;
  }

  // ---------------------------------------------------------------------------------//
  int getAttributes ()
  // ---------------------------------------------------------------------------------//
  {
    return attributes;
  }

  // ---------------------------------------------------------------------------------//
  int getFileNameLength ()
  // ---------------------------------------------------------------------------------//
  {
    return fileNameLength;
  }

  // ---------------------------------------------------------------------------------//
  int getTotalThreads ()
  // ---------------------------------------------------------------------------------//
  {
    return totThreads;
  }

  // ---------------------------------------------------------------------------------//
  boolean hasDisk ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasDisk ())
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  boolean hasFile ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasFile ())
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  boolean hasFile (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasFile (fileName))
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  boolean hasResource ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasResource ())
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  String getFileName ()
  // ---------------------------------------------------------------------------------//
  {
    if (fileNameLength > 0)                 // probably version 0
      return fileName;

    for (Thread thread : threads)
      if (thread.hasFileName ())
      {
        String fileName = thread.getFileName ();
        if (separator != '/')
          return fileName.replace (separator, '/');
        return thread.getFileName ();
      }

    return "";
  }

  // ---------------------------------------------------------------------------------//
  int getFileType ()
  // ---------------------------------------------------------------------------------//
  {
    return fileType;
  }

  // ---------------------------------------------------------------------------------//
  int getAuxType ()
  // ---------------------------------------------------------------------------------//
  {
    return auxType;
  }

  // ---------------------------------------------------------------------------------//
  LocalDateTime getCreated ()
  // ---------------------------------------------------------------------------------//
  {
    return created == null ? null : created.getLocalDateTime ();
  }

  // ---------------------------------------------------------------------------------//
  LocalDateTime getModified ()
  // ---------------------------------------------------------------------------------//
  {
    return modified == null ? null : modified.getLocalDateTime ();
  }

  // ---------------------------------------------------------------------------------//
  LocalDateTime getArchived ()
  // ---------------------------------------------------------------------------------//
  {
    return archived == null ? null : archived.getLocalDateTime ();
  }

  // ---------------------------------------------------------------------------------//
  int getFileSystemID ()
  // ---------------------------------------------------------------------------------//
  {
    return fileSystemID;
  }

  // ---------------------------------------------------------------------------------//
  String getFileSystemName ()
  // ---------------------------------------------------------------------------------//
  {
    return fileSystems[fileSystemID];
  }

  // Called by NuFX.listFiles()
  // ---------------------------------------------------------------------------------//
  int getFileSize ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasFile ())
        return thread.getFileSize ();

    return 0;
  }

  // ---------------------------------------------------------------------------------//
  int getThreadFormat ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasFile () || thread.hasDisk ())
        return thread.threadFormat;

    return 0;
  }

  // ---------------------------------------------------------------------------------//
  String getThreadFormatText ()
  // ---------------------------------------------------------------------------------//
  {
    return threadFormats[getThreadFormat ()];
  }

  // ---------------------------------------------------------------------------------//
  int getUncompressedSize ()
  // ---------------------------------------------------------------------------------//
  {
    if (hasDisk ())
      return auxType * storType;

    int size = 0;

    for (Thread thread : threads)
      if (thread.hasFile () || thread.hasResource () || thread.hasDisk ())
        size += thread.getUncompressedEOF ();

    return size;
  }

  // ---------------------------------------------------------------------------------//
  int getCompressedSize ()
  // ---------------------------------------------------------------------------------//
  {
    int size = 0;

    for (Thread thread : threads)
      if (thread.hasFile () || thread.hasResource () || thread.hasDisk ())
        size += thread.getCompressedEOF ();

    return size;
  }

  // ---------------------------------------------------------------------------------//
  public float getCompressedPct ()
  // ---------------------------------------------------------------------------------//
  {
    float pct = 100;
    if (getUncompressedSize () > 0)
      pct = getCompressedSize () * 100 / getUncompressedSize ();

    return pct;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getData ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasFile ())
        return thread.getData ();

    return null;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getResourceData ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasResource ())
        return thread.getData ();

    return null;
  }

  // ---------------------------------------------------------------------------------//
  String getLine ()
  // ---------------------------------------------------------------------------------//
  {
    String name = getFileName ();
    if (name.length () > 27)
      name = ".." + name.substring (name.length () - 25);

    //    float pct = 100;
    //    if (getUncompressedSize () > 0)
    //      pct = getCompressedSize () * 100 / getUncompressedSize ();

    String lockedFlag = (access | 0xC3) == 1 ? "+" : " ";
    String forkedFlag = hasResource () ? "+" : " ";

    if (hasDisk ())
      return String.format ("%s%-27.27s %-4s %-6s %-15s  %s  %3.0f%%   %7d", lockedFlag,
          name, "Disk", (getUncompressedSize () / 1024) + "k", archived.format2 (),
          getThreadFormatText (), getCompressedPct (), getUncompressedSize ());

    return String.format ("%s%-27.27s %s%s $%04X  %-15s  %s  %3.0f%%   %7d", lockedFlag,
        name, fileTypes[fileType], forkedFlag, auxType, archived.format2 (),
        getThreadFormatText (), getCompressedPct (), getUncompressedSize ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    String bits = "00000000" + Integer.toBinaryString (access & 0xFF);
    bits = bits.substring (bits.length () - 8);
    String decode = Utility.matchFlags (access, accessChars);

    text.append (String.format ("Header CRC ..... %,d  (%<04X)%n", crc));
    text.append (String.format ("Attributes ..... %d%n", attributes));
    text.append (String.format ("Version ........ %d%n", version));
    text.append (String.format ("Threads ........ %d%n", totThreads));
    text.append (
        String.format ("File sys id .... %d (%s)%n", fileSystemID, getFileSystemName ()));
    text.append (String.format ("Separator ...... %s%n", separator));
    text.append (String.format ("Access ......... %s  %s%n", bits, decode));

    if (storType < 16)
    {
      text.append (String.format ("File type ...... %02X     %s%n", fileType,
          fileTypes[fileType]));
      text.append (String.format ("Aux type ....... %,d  $%<04X%n", auxType));
      text.append (
          String.format ("Stor type ...... %,d  %s%n", storType, storage[storType]));
    }
    else
    {
      text.append (String.format ("Zero ........... %,d%n", fileType));
      text.append (String.format ("Total blocks ... %,d%n", auxType));
      text.append (String.format ("Block size ..... %,d%n", storType));
    }

    text.append (String.format ("Created ........ %s%n", created.format ()));
    text.append (String.format ("Modified ....... %s%n", modified.format ()));
    text.append (String.format ("Archived ....... %s%n", archived.format ()));
    text.append (String.format ("Option size .... %,d%n", optionSize));
    text.append (String.format ("Filename len ... %,d%n", fileNameLength));
    text.append (String.format ("Filename ....... %s", fileName));

    return text.toString ();
  }
}
