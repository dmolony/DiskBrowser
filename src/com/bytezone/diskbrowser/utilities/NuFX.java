package com.bytezone.diskbrowser.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.prodos.write.DiskFullException;
import com.bytezone.diskbrowser.prodos.write.ProdosDisk;

// -----------------------------------------------------------------------------------//
public class NuFX
// -----------------------------------------------------------------------------------//
{
  private MasterHeader masterHeader;
  private final byte[] buffer;
  private final boolean debug = false;

  private final List<Record> records = new ArrayList<> ();
  private int totalFiles;
  private int totalDisks;

  // ---------------------------------------------------------------------------------//
  public NuFX (Path path) throws FileFormatException, IOException
  // ---------------------------------------------------------------------------------//
  {
    buffer = Files.readAllBytes (path);

    masterHeader = new MasterHeader (buffer);

    int dataPtr = 48;
    if (masterHeader.bin2)
      dataPtr += 128;

    if (debug)
      System.out.printf ("%s%n%n", masterHeader);

    for (int rec = 0; rec < masterHeader.getTotalRecords (); rec++)
    {
      Record record = new Record (buffer, dataPtr);
      records.add (record);

      if (debug)
        System.out.printf ("Record: %d%n%n%s%n%n", rec, record);

      dataPtr += record.getAttributes () + record.getFileNameLength ();
      int threadsPtr = dataPtr;
      dataPtr += record.getTotalThreads () * 16;

      for (int i = 0; i < record.getTotalThreads (); i++)
      {
        Thread thread = new Thread (buffer, threadsPtr + i * 16, dataPtr);
        record.threads.add (thread);
        dataPtr += thread.getCompressedEOF ();

        if (debug)
          System.out.printf ("Thread: %d%n%n%s%n%n", i, thread);
      }

      if (record.hasFile ())
        ++totalFiles;
      if (record.hasDisk ())
        ++totalDisks;
    }
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getDiskBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    if (totalDisks > 0)
    {
      for (Record record : records)
        for (Thread thread : record.threads)
          if (thread.hasDisk ())
            return thread.getData ();
    }
    else if (totalFiles > 0)
    {
      try
      {
        ProdosDisk disk = new ProdosDisk (3200, "DISKBROWSER");
        int count = 0;
        for (Record record : records)
        {
          if (record.hasFile ())
          {
            String fileName = record.getFileName ();
            int fileSize = record.getFileSize ();
            byte fileType = (byte) record.getFileType ();
            int eof = record.getUncompressedSize ();
            int auxType = record.getAuxType ();
            LocalDateTime created = record.getCreated ();
            LocalDateTime modified = record.getModified ();
            byte[] buffer = record.getData ();

            System.out.printf ("%3d %-35s %,7d  %02X  %,7d  %,7d  %,7d  %s  %s%n",
                ++count, fileName, fileSize, fileType, eof, auxType, buffer.length,
                created, modified);
            disk.addFile (fileName, fileType, auxType, created, modified, buffer);
          }
        }

        disk.close ();

        return disk.getBuffer ();
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
      catch (DiskFullException e)
      {
        e.printStackTrace ();
      }
    }

    return null;
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getFileBuffer (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    for (Record record : records)
      if (record.hasFile (fileName))
        for (Thread thread : record.threads)
          if (thread.hasFile ())
            return thread.getData ();

    return null;
  }

  // ---------------------------------------------------------------------------------//
  public int getTotalFiles ()
  // ---------------------------------------------------------------------------------//
  {
    return totalFiles;
  }

  // ---------------------------------------------------------------------------------//
  public int getTotalDisks ()
  // ---------------------------------------------------------------------------------//
  {
    return totalDisks;
  }

  // ---------------------------------------------------------------------------------//
  private void listFiles ()
  // ---------------------------------------------------------------------------------//
  {
    int count = 0;
    for (Record record : records)
    {
      if (record.hasFile ())
        System.out.printf ("%3d %-35s %,7d  %d  %,7d%n", count, record.getFileName (),
            record.getFileSize (), record.getFileType (), record.getUncompressedSize ());
      count++;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    for (Record record : records)
      for (Thread thread : record.threads)
        if (thread.hasDisk ())
          return thread.toString ();

    return "no disk";
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args) throws FileFormatException, IOException
  // ---------------------------------------------------------------------------------//
  {
    File file = new File ("/Users/denismolony/Dropbox/Examples/SHK/DiversiCopy.3.1.shk");

    NuFX nufx = new NuFX (file.toPath ());
    System.out.println (nufx);
  }
}