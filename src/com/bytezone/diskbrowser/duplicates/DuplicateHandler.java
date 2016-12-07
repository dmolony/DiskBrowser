package com.bytezone.diskbrowser.duplicates;

import java.io.File;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.gui.FileComparator;
import com.bytezone.diskbrowser.utilities.Utility;

public class DuplicateHandler
{
  private static String spaces = "                                                   ";
  private static final FileComparator fileComparator = new FileComparator ();
  private static final int MAX_NAME_WIDTH = 34;
  private static final String FORMAT = "%-" + MAX_NAME_WIDTH + "s %,10d%n";

  private final File rootFolder;
  private int totalDisks;
  private int totalFolders;

  // total files for each suffix
  private final Map<String, Integer> typeList = new TreeMap<String, Integer> ();

  // list of unique disk names -> List of File duplicates
  final Map<String, List<DiskDetails>> duplicateDisks =
      new TreeMap<String, List<DiskDetails>> ();

  // list of unique disk names -> File
  private final Map<String, File> diskNames = new HashMap<String, File> ();

  // list of checksum -> File
  final Map<Long, List<File>> dosMap = new TreeMap<Long, List<File>> ();

  public DuplicateHandler (File rootFolder)
  {
    this.rootFolder = rootFolder;
    countDisks ();
  }

  void countDisks ()
  {
    traverse (rootFolder, 0);

    System.out.printf ("%nFolders ..... %,5d%n", totalFolders);
    System.out.printf ("Disks ....... %,5d%n%n", totalDisks);

    int grandTotal = 0;
    for (String key : typeList.keySet ())
    {
      int typeTotal = typeList.get (key);
      grandTotal += typeTotal;
      System.out.printf ("%13.13s %,6d%n", key + " ...........", typeTotal);
    }
    System.out.printf ("%nTotal ....... %,6d%n%n", grandTotal);
  }

  private void traverse (File directory, int depth)
  {
    File[] files = directory.listFiles ();
    if (files == null || files.length == 0)
    {
      System.out.println ("Empty folder : " + directory.getAbsolutePath ());
      return;
    }

    Arrays.sort (files, fileComparator);

    System.out.printf ("%nFolder: %s%n%n",
        directory.getAbsolutePath ().substring (rootFolder.getAbsolutePath ().length ()));

    for (File file : files)
    {
      if (file.isDirectory ())
      {
        ++totalFolders;
        traverse (file, depth + 1);
      }
      else if (Utility.validFileType (file.getName ()))
      {
        if (file.getName ().endsWith (".gz") && !file.getName ().endsWith ("dsk.gz"))
          continue;

        ++totalDisks;

        String name = file.getName ();
        int nameLength = name.length ();
        if (nameLength > MAX_NAME_WIDTH)
          name = name.substring (0, 15) + "..."
              + name.substring (nameLength - MAX_NAME_WIDTH + 18);

        System.out.printf (FORMAT, name, file.length ());
      }
    }
  }

  private void checksumDos (File file)
  {
    if (file.length () != 143360 || file.getAbsolutePath ().contains ("/ZDisks/"))
      return;

    Disk disk = new AppleDisk (file, 35, 16);
    byte[] buffer = disk.readSector (0, 0);

    Checksum checksum = new CRC32 ();
    checksum.update (buffer, 0, buffer.length);
    long cs = checksum.getValue ();
    List<File> files = dosMap.get (cs);
    if (files == null)
    {
      files = new ArrayList<File> ();
      dosMap.put (cs, files);
    }
    files.add (file);
  }

  private void checkDuplicates (File file)
  {
    if (diskNames.containsKey (file.getName ()))
    {
      List<DiskDetails> diskList = duplicateDisks.get (file.getName ());
      if (diskList == null)
      {
        diskList = new ArrayList<DiskDetails> ();
        duplicateDisks.put (file.getName (), diskList);
        diskList.add (new DiskDetails (diskNames.get (file.getName ())));// add original
      }
      diskList.add (new DiskDetails (file));                        // add the duplicate
    }
    else
      diskNames.put (file.getName (), file);
  }

  public static void main (String[] args)
  {
    DuplicateHandler dh = new DuplicateHandler (
        new File ("/Users/denismolony/Apple II stuff/AppleDisk Images II/apple disks"));
  }
}