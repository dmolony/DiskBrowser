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
  private int totalBlocks;

  private VolumeName volumeName = new VolumeName ();

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
      {
        ++totalFiles;

        // note: total blocks does not include subdirectory blocks
        int blocks = (record.getFileSize () - 1) / 512 + 1;
        if (blocks == 1)                      // seedling
          totalBlocks += blocks;
        else if (blocks <= 256)               // sapling
          totalBlocks += blocks + 1;
        else                                  // tree
          totalBlocks += blocks + (blocks / 256) + 2;

        volumeName.storePath (record.getFileName ());
      }

      if (record.hasDisk ())
        ++totalDisks;
    }

    //    volumeName.info ();
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

    if (totalFiles > 0)
    {
      // should check that files are all in prodos format

      int[] diskSizes = { 280, 800, 1600, 3200, 6400, 65536 };
      for (int diskSize : diskSizes)      // in case we choose a size that is too small
      {
        if (diskSize < (totalBlocks + 10))
          continue;

        try
        {
          ProdosDisk disk = new ProdosDisk (diskSize, volumeName.getVolumeName ());
          int count = 0;

          for (Record record : records)
          {
            if (record.hasFile ())
            {
              String fileName = volumeName.convert (record.getFileName ());
              //              int fileSize = record.getFileSize ();
              byte fileType = (byte) record.getFileType ();
              int eof = record.getUncompressedSize ();
              int auxType = record.getAuxType ();
              LocalDateTime created = record.getCreated ();
              LocalDateTime modified = record.getModified ();
              byte[] buffer = record.getData ();

              if (false)
                System.out.printf ("%3d %-35s %02X %,7d %,7d %,7d  %s  %s%n", ++count,
                    fileName, fileType, auxType, eof, buffer.length, created, modified);

              disk.addFile (fileName, fileType, auxType, created, modified, buffer);
            }
          }

          disk.close ();

          return disk.getBuffer ();
        }
        catch (IOException e)
        {
          e.printStackTrace ();
          return null;
        }
        catch (DiskFullException e)
        {
          System.out.println ("disk full: " + diskSize);    // go round again
        }
      }
    }

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
  public int getTotalBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return totalBlocks;
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
  class VolumeName
  // ---------------------------------------------------------------------------------//
  {
    private List<String> paths = new ArrayList<> ();
    private boolean rootContainsFiles;

    private String volumeName = "DiskBrowser";
    private int nameOffset = 0;

    // -------------------------------------------------------------------------------//
    private void storePath (String fileName)
    // -------------------------------------------------------------------------------//
    {
      int pos = fileName.lastIndexOf ('/');
      if (pos < 0)
        rootContainsFiles = true;
      else
      {
        String path = fileName.substring (0, pos);
        for (int i = 0; i < paths.size (); i++)
        {
          String cmp = paths.get (i);
          if (cmp.startsWith (path))        // longer path already there
            return;
          if (path.startsWith (cmp))
          {
            paths.set (i, path);            // replace shorter path with longer path
            return;
          }
        }
        paths.add (path);
      }
    }

    // -------------------------------------------------------------------------------//
    private String getVolumeName ()
    // -------------------------------------------------------------------------------//
    {
      if (rootContainsFiles)
        return volumeName;

      if (paths.size () > 0)
      {
        int pos = paths.get (0).indexOf ('/');
        if (pos > 0)
        {
          String firstPath = paths.get (0).substring (0, pos + 1);
          System.out.println (firstPath);

          boolean allSame = true;
          for (String pathName : paths)
            if (!pathName.startsWith (firstPath))
            {
              allSame = false;
              break;
            }

          if (allSame)
          {
            volumeName = firstPath.substring (0, pos);
            nameOffset = volumeName.length () + 1;      // skip volume name in all paths
          }
        }
      }

      if (paths.size () == 1)                         // exactly one directory path
      {
        String onlyPath = paths.get (0);
        int pos = onlyPath.indexOf ('/');
        if (pos == -1)                                // no separators
          volumeName = onlyPath;
        else                                          // use first component
          volumeName = onlyPath.substring (0, pos);
        nameOffset = volumeName.length () + 1;        // skip volume name in all paths
      }

      return volumeName;
    }

    // -------------------------------------------------------------------------------//
    String convert (String fileName)
    // -------------------------------------------------------------------------------//
    {
      if (nameOffset > 0)         // remove volume name from path
        return fileName.substring (nameOffset);

      return fileName;
    }

    // -------------------------------------------------------------------------------//
    void info ()
    // -------------------------------------------------------------------------------//
    {
      if (rootContainsFiles)
        System.out.println ("Root contains files");
      System.out.println ("Unique paths:");
      if (paths.size () == 0)
        System.out.println ("<none>");
      for (String pathName : paths)
        System.out.println (pathName);
    }
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args) throws FileFormatException, IOException
  // ---------------------------------------------------------------------------------//
  {
    File file = new File (
        "/Users/denismolony/Dropbox/Examples/SHK/Disk Disintegrator Deluxe 5.0_D1.SHK");

    NuFX nufx = new NuFX (file.toPath ());
    System.out.println (nufx);
  }
}