package com.bytezone.diskbrowser.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.prodos.write.DiskFullException;
import com.bytezone.diskbrowser.prodos.write.FileAlreadyExistsException;
import com.bytezone.diskbrowser.prodos.write.FileEntry;
import com.bytezone.diskbrowser.prodos.write.ProdosDisk;
import com.bytezone.diskbrowser.prodos.write.VolumeCatalogFullException;

// -----------------------------------------------------------------------------------//
public class NuFX
// -----------------------------------------------------------------------------------//
{
  private static final String UNDERLINE =
      "------------------------------------------------------"
          + "-----------------------";
  private MasterHeader masterHeader;
  private final byte[] buffer;
  private final boolean debug = false;

  private final List<Record> records = new ArrayList<> ();
  private int totalFiles;
  private int totalDisks;
  private int totalBlocks;

  private VolumeName volumeName;

  // ---------------------------------------------------------------------------------//
  public NuFX (Path path) throws FileFormatException, IOException
  // ---------------------------------------------------------------------------------//
  {
    buffer = Files.readAllBytes (path);
    volumeName = new VolumeName (path);

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
        volumeName.storePath (record.getFileName ());
      }

      if (record.hasDisk ())
        ++totalDisks;
    }

    printSummary ();
  }

  // ---------------------------------------------------------------------------------//
  void printSummary ()
  // ---------------------------------------------------------------------------------//
  {
    System.out.printf (" %s   Created:%s   Mod:%s     Recs:%5d%n%n",
        volumeName.getFileName (), masterHeader.getCreated2 (),
        masterHeader.getModified2 (), masterHeader.getTotalRecords ());
    System.out.println (" Name                        Type Auxtyp Archived"
        + "         Fmat Size Un-Length");
    System.out.println (UNDERLINE);

    int totalUncompressedSize = 0;
    int totalCompressedSize = 0;

    for (Record record : records)
    {
      System.out.println (record.getLine ());
      totalUncompressedSize += record.getUncompressedSize ();
      totalCompressedSize += record.getCompressedSize ();
    }
    System.out.println (UNDERLINE);

    float pct = 0;
    if (totalUncompressedSize > 0)
      pct = totalCompressedSize * 100 / totalUncompressedSize;
    System.out.printf (" Uncomp:%7d  Comp:%7d  %%of orig:%3.0f%%%n%n",
        totalUncompressedSize, totalCompressedSize, pct);
  }

  // ---------------------------------------------------------------------------------//
  private void calculateTotalBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    totalBlocks = 0;

    for (Record record : records)
      if (record.hasFile ())
      {
        // note: total blocks does not include subdirectory blocks
        int blocks = (record.getUncompressedSize () - 1) / 512 + 1;
        if (blocks == 1)                      // seedling
          totalBlocks += blocks;
        else if (blocks <= 256)               // sapling
          totalBlocks += blocks + 1;
        else                                  // tree
          totalBlocks += blocks + (blocks / 256) + 2;
      }
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getDiskBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    if (totalDisks > 0)
    {
      if (debug)
        System.out.println ("Reading disk");

      for (Record record : records)
        for (Thread thread : record.threads)
          if (thread.hasDisk ())
            return thread.getData ();
    }

    if (totalFiles > 0)
    {
      if (debug)
        System.out.println ("Reading files");

      calculateTotalBlocks ();
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

              if (!record.isValidFileSystem ())
              {
                System.out.printf ("File %s is file system %s%n", fileName,
                    record.getFileSystemName ());
                continue;
              }

              //              int fileSize = record.getFileSize ();
              byte fileType = (byte) record.getFileType ();
              int eof = record.getUncompressedSize ();
              int auxType = record.getAuxType ();
              LocalDateTime created = record.getCreated ();
              LocalDateTime modified = record.getModified ();
              byte[] buffer = record.getData ();

              if (debug)
                System.out.printf ("%3d %-35s %02X %,7d %,7d %,7d  %s  %s%n", ++count,
                    fileName, fileType, auxType, eof, buffer.length, created, modified);

              FileEntry fileEntry;
              try
              {
                fileEntry = disk.addFile (fileName, fileType, auxType, created, modified,
                    buffer, eof);
              }
              catch (FileAlreadyExistsException e)
              {
                System.out.printf ("File %s not added%n", fileName);
                break;
              }

              if (record.hasResource ())
              {
                buffer = record.getResourceData ();
                disk.addResourceFork (fileEntry, buffer, buffer.length);
              }
            }
          }

          disk.close ();

          return disk.getBuffer ();
        }
        catch (DiskFullException e)
        {
          System.out.println ("disk full: " + diskSize);    // go round again
        }
        catch (VolumeCatalogFullException e)
        {
          e.printStackTrace ();
          return null;
        }
        catch (IOException e)
        {
          e.printStackTrace ();
          return null;
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
      {
        System.out.printf ("%3d %-35s %,7d  %d  %,7d%n", count, record.getFileName (),
            record.getFileSize (), record.getFileType (), record.getUncompressedSize ());
      }
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
    private Path path;

    // -------------------------------------------------------------------------------//
    VolumeName (Path path)
    // -------------------------------------------------------------------------------//
    {
      this.path = path;
      volumeName = getFileName ();
      int pos = volumeName.lastIndexOf ('.');
      if (pos > 0)
        volumeName = volumeName.substring (0, pos);
      if (volumeName.length () > 15)
        volumeName = volumeName.substring (0, 15);
      volumeName = volumeName.replace (' ', '.');
    }

    // -------------------------------------------------------------------------------//
    String getFileName ()
    // -------------------------------------------------------------------------------//
    {
      return path.getFileName ().toString ();
    }

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
      //      if (true)
      //        return volumeName;

      if (rootContainsFiles)
        return volumeName;

      if (paths.size () > 0)
      {
        int pos = paths.get (0).indexOf ('/');
        if (pos > 0)
        {
          String firstPath = paths.get (0).substring (0, pos + 1);

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
}