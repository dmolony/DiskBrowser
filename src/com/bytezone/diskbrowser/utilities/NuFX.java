package com.bytezone.diskbrowser.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;

public class NuFX
{
  private static String[] fileSystems =
      {//
       "", "ProDOS/SOS", "DOS 3.3", "DOS 3.2", "Apple II Pascal", "Macintosh HFS",
       "Macintosh MFS", "Lisa File System", "Apple CP/M", "", "MS-DOS", "High Sierra",
       "ISO 9660", "AppleShare" };
  private Header header;
  private final byte[] buffer;
  private final boolean debug = false;

  private final List<Record> records = new ArrayList<Record> ();
  private final List<Thread> threads = new ArrayList<Thread> ();

  public NuFX (Path path) throws FileFormatException, IOException
  {
    buffer = Files.readAllBytes (path);
    readBuffer ();
  }

  public NuFX (File file) throws FileFormatException, IOException
  {
    buffer = Files.readAllBytes (file.toPath ());
    readBuffer ();
  }

  private void readBuffer ()
  {
    header = new Header (buffer);

    int dataPtr = 48;
    if (header.bin2)
      dataPtr += 128;

    if (debug)
      System.out.printf ("%s%n%n", header);

    for (int rec = 0; rec < header.totalRecords; rec++)
    {
      Record record = new Record (dataPtr);
      records.add (record);

      if (debug)
        System.out.printf ("Record: %d%n%n%s%n%n", rec, record);

      dataPtr += record.attributes + record.fileNameLength;
      int threadsPtr = dataPtr;
      dataPtr += record.totThreads * 16;

      for (int i = 0; i < record.totThreads; i++)
      {
        Thread thread = new Thread (buffer, threadsPtr + i * 16, dataPtr);
        threads.add (thread);
        dataPtr += thread.getCompressedEOF ();

        if (debug)
          System.out.printf ("Thread: %d%n%n%s%n%n", i, thread);
      }
    }
  }

  public byte[] getBuffer ()
  {
    for (Thread thread : threads)
      if (thread.hasDisk ())
        return thread.getData ();
    return null;
  }

  @Override
  public String toString ()
  {
    for (Thread thread : threads)
      if (thread.hasDisk ())
        return thread.toString ();
    return "no disk";
  }

  protected static int getCRC (final byte[] buffer, int base)
  {
    int crc = base;
    for (int j = 0; j < buffer.length; j++)
    {
      crc = ((crc >>> 8) | (crc << 8)) & 0xFFFF;
      crc ^= (buffer[j] & 0xFF);
      crc ^= ((crc & 0xFF) >> 4);
      crc ^= (crc << 12) & 0xFFFF;
      crc ^= ((crc & 0xFF) << 5) & 0xFFFF;
    }

    crc &= 0xFFFF;
    return crc;
  }

  class Header
  {
    private final int totalRecords;
    private final int version;
    private final int eof;
    private final int crc;
    private final DateTime created;
    private final DateTime modified;
    boolean bin2;

    public Header (byte[] buffer) throws FileFormatException
    {
      int ptr = 0;

      while (true)
      {
        if (isNuFile (buffer, ptr))
          break;

        if (isBin2 (buffer, ptr))
        {
          ptr += 128;
          bin2 = true;
          continue;
        }

        throw new FileFormatException ("NuFile not found");
      }

      crc = Utility.getWord (buffer, ptr + 6);
      totalRecords = Utility.getLong (buffer, ptr + 8);
      created = new DateTime (buffer, ptr + 12);
      modified = new DateTime (buffer, ptr + 20);
      version = Utility.getWord (buffer, ptr + 28);
      eof = Utility.getLong (buffer, ptr + 38);

      byte[] crcBuffer = new byte[40];
      System.arraycopy (buffer, ptr + 8, crcBuffer, 0, crcBuffer.length);
      if (crc != getCRC (crcBuffer, 0))
      {
        System.out.println ("***** Master CRC mismatch *****");
        throw new FileFormatException ("Master CRC failed");
      }
    }

    private boolean isNuFile (byte[] buffer, int ptr)
    {
      if (buffer[ptr] == 0x4E && buffer[ptr + 1] == (byte) 0xF5 && buffer[ptr + 2] == 0x46
          && buffer[ptr + 3] == (byte) 0xE9 && buffer[ptr + 4] == 0x6C
          && buffer[ptr + 5] == (byte) 0xE5)
        return true;
      return false;
    }

    private boolean isBin2 (byte[] buffer, int ptr)
    {
      if (buffer[ptr] == 0x0A && buffer[ptr + 1] == 0x47 && buffer[ptr + 2] == 0x4C
          && buffer[ptr + 18] == (byte) 0x02)
        return true;
      return false;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Master CRC ..... %,d  (%04X)%n", crc, crc));
      text.append (String.format ("Records ........ %,d%n", totalRecords));
      text.append (String.format ("Created ........ %s%n", created.format ()));
      text.append (String.format ("Modified ....... %s%n", modified.format ()));
      text.append (String.format ("Version ........ %,d%n", version));
      text.append (String.format ("Master EOF ..... %,d", eof));

      return text.toString ();
    }
  }

  class Record
  {
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

    public Record (int dataPtr) throws FileFormatException
    {
      // check for NuFX
      if (!isNuFX (buffer, dataPtr))
        throw new FileFormatException ("NuFX not found");

      crc = Utility.getWord (buffer, dataPtr + 4);
      attributes = Utility.getWord (buffer, dataPtr + 6);
      version = Utility.getWord (buffer, dataPtr + 8);
      totThreads = Utility.getLong (buffer, dataPtr + 10);
      fileSystemID = Utility.getWord (buffer, dataPtr + 14);
      separator = (char) (buffer[dataPtr + 16] & 0x00FF);
      access = Utility.getLong (buffer, dataPtr + 18);
      fileType = Utility.getLong (buffer, dataPtr + 22);
      auxType = Utility.getLong (buffer, dataPtr + 26);
      storType = Utility.getWord (buffer, dataPtr + 30);
      created = new DateTime (buffer, dataPtr + 32);
      modified = new DateTime (buffer, dataPtr + 40);
      archived = new DateTime (buffer, dataPtr + 48);
      optionSize = Utility.getWord (buffer, dataPtr + 56);
      fileNameLength = Utility.getWord (buffer, dataPtr + attributes - 2);

      int len = attributes + fileNameLength - 6;
      byte[] crcBuffer = new byte[len + totThreads * 16];
      System.arraycopy (buffer, dataPtr + 6, crcBuffer, 0, crcBuffer.length);

      if (crc != getCRC (crcBuffer, 0))
      {
        System.out.println ("***** Header CRC mismatch *****");
        throw new FileFormatException ("Header CRC failed");
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

    private boolean isNuFX (byte[] buffer, int ptr)
    {
      if (buffer[ptr] == 0x4E && buffer[ptr + 1] == (byte) 0xF5 && buffer[ptr + 2] == 0x46
          && buffer[ptr + 3] == (byte) 0xD8)
        return true;
      return false;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Header CRC ..... %,d  (%04X)%n", crc, crc));
      text.append (String.format ("Attributes ..... %d%n", attributes));
      text.append (String.format ("Version ........ %d%n", version));
      text.append (String.format ("Threads ........ %d%n", totThreads));
      text.append (String.format ("File sys id .... %d (%s)%n", fileSystemID,
          fileSystems[fileSystemID]));
      text.append (String.format ("Separator ...... %s%n", separator));
      text.append (String.format ("Access ......... %,d%n", access));
      text.append (String.format ("File type ...... %,d%n", fileType));
      text.append (String.format ("Aux type ....... %,d%n", auxType));
      text.append (String.format ("Stor type ...... %,d%n", storType));
      text.append (String.format ("Created ........ %s%n", created.format ()));
      text.append (String.format ("Modified ....... %s%n", modified.format ()));
      text.append (String.format ("Archived ....... %s%n", archived.format ()));
      text.append (String.format ("Option size .... %,d%n", optionSize));
      text.append (String.format ("Filename len ... %,d%n", fileNameLength));
      text.append (String.format ("Filename ....... %s", fileName));

      return text.toString ();
    }
  }
}