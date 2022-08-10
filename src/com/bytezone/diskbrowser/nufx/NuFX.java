package com.bytezone.diskbrowser.nufx;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.BLOCK_SIZE;

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
import com.bytezone.diskbrowser.utilities.FileFormatException;

// -----------------------------------------------------------------------------------//
public class NuFX
// -----------------------------------------------------------------------------------//
{
  private static final String UNDERLINE =
      "------------------------------------------------------" + "-----------------------";
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
    volumeName = new VolumeName (path.getFileName ().toString ());
    read (buffer);
  }

  // ---------------------------------------------------------------------------------//
  public NuFX (byte[] buffer, String fileName)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;
    this.volumeName = new VolumeName (fileName);

    read (buffer);
  }

  // ---------------------------------------------------------------------------------//
  void read (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    masterHeader = new MasterHeader (buffer);

    int dataPtr = masterHeader.bin2 ? 176 : 48;

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
  }

  // ---------------------------------------------------------------------------------//
  private void calculateTotalBlocks ()        // not exact
  // ---------------------------------------------------------------------------------//
  {
    totalBlocks = 0;

    for (Record record : records)
      if (record.hasFile () || record.hasResource ())
      {
        // note: total blocks does not include subdirectory blocks
        int blocks = (record.getUncompressedSize () - 1) / BLOCK_SIZE + 1;
        if (blocks == 1)                      // seedling
          totalBlocks += blocks;
        else if (blocks <= 0x100)               // sapling
          totalBlocks += blocks + 1;
        else                                  // tree
          totalBlocks += blocks + (blocks / 0x100) + 2;
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

    if (totalFiles == 0)
      return null;

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
            byte[] buffer;
            try
            {
              buffer = record.getData ();
            }
            catch (Exception e)
            {
              System.out.println (e.getMessage ());
              System.out.printf ("Failed to unpack: %s%n", fileName);
              continue;
            }

            if (debug)
              System.out.printf ("%3d %-35s %02X %,7d %,7d %,7d  %s  %s%n", ++count, fileName,
                  fileType, auxType, eof, buffer.length, created, modified);

            FileEntry fileEntry;
            try
            {
              fileEntry =
                  disk.addFile (fileName, fileType, auxType, created, modified, buffer, eof);
            }
            catch (FileAlreadyExistsException e)
            {
              System.out.printf ("File %s not added%n", fileName);
              break;
            }

            if (record.hasResource ())
            {
              try
              {
                buffer = record.getResourceData ();
                disk.addResourceFork (fileEntry, buffer, buffer.length);
              }
              catch (Exception e)
              {
                System.out.println (e.getMessage ());
                System.out.printf ("Failed to unpack resource fork: %s%n", fileName);
              }
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
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format (" %-15.15s Created:%-17s Mod:%-17s   Recs:%5d%n%n",
        volumeName.volumeName, masterHeader.getCreated2 (), masterHeader.getModified2 (),
        masterHeader.getTotalRecords ()));

    text.append (
        " Name                        Type Auxtyp Archived" + "         Fmat Size Un-Length\n");

    text.append (String.format ("%s%n", UNDERLINE));

    int totalUncompressedSize = 0;
    int totalCompressedSize = 0;

    for (Record record : records)
    {
      text.append (String.format ("%s%n", record.getLine ()));
      totalUncompressedSize += record.getUncompressedSize ();
      totalCompressedSize += record.getCompressedSize ();
    }

    text.append (String.format ("%s%n", UNDERLINE));

    float pct = 0;
    if (totalUncompressedSize > 0)
      pct = totalCompressedSize * 100 / totalUncompressedSize;
    text.append (String.format (" Uncomp:%7d  Comp:%7d  %%of orig:%3.0f%%%n%n",
        totalUncompressedSize, totalCompressedSize, pct));

    return text.toString ();
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
    VolumeName (String name)
    // -------------------------------------------------------------------------------//
    {
      int pos = name.lastIndexOf ('.');
      if (pos > 0)
        name = name.substring (0, pos);
      if (name.length () > 15)
        name = name.substring (0, 15);
      name = name.replace (' ', '.');

      this.volumeName = name;
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