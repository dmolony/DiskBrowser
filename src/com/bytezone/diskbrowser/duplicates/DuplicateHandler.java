package com.bytezone.diskbrowser.duplicates;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.bytezone.diskbrowser.utilities.Utility;

public class DuplicateHandler
{
  //  private static final FileComparator fileComparator = new FileComparator ();
  //  private static final int MAX_NAME_WIDTH = 34;
  //  private static final String FORMAT = "%-" + MAX_NAME_WIDTH + "s %,10d%n";

  private final File rootFolder;
  private int totalDisks;
  private int totalFolders;
  private final int rootFolderNameLength;

  //  private final boolean debug = false;

  // total files for each suffix
  private final Map<String, Integer> typeList = new TreeMap<String, Integer> ();

  // list of checksum -> DiskDetails
  final Map<Long, DiskDetails> checksumMap = new HashMap<Long, DiskDetails> ();

  // list of unique disk names -> File
  private final Map<String, DiskDetails> fileNameMap =
      new TreeMap<String, DiskDetails> ();

  public DuplicateHandler (File rootFolder)
  {
    this.rootFolder = rootFolder;
    rootFolderNameLength = rootFolder.getAbsolutePath ().length ();
  }

  public Map<String, DiskDetails> getFileNameMap ()
  {
    return fileNameMap;
  }

  public Map<Long, DiskDetails> getChecksumMap ()
  {
    return checksumMap;
  }

  void countDisks ()
  {
    traverse (rootFolder);

    System.out.printf ("%nFolders ..... %,7d%n", totalFolders);
    System.out.printf ("Disks ....... %,7d%n%n", totalDisks);

    int grandTotal = 0;
    for (String key : typeList.keySet ())
    {
      int typeTotal = typeList.get (key);
      grandTotal += typeTotal;
      System.out.printf ("%13.13s %,7d%n", key + " ...........", typeTotal);
    }
    System.out.printf ("%nTotal ....... %,7d%n%n", grandTotal);
  }

  File getRootFolder ()
  {
    return rootFolder;
  }

  private void traverse (File directory)
  {
    File[] files = directory.listFiles ();

    if (files == null || files.length == 0)
    {
      System.out.println ("Empty folder : " + directory.getAbsolutePath ());
      return;
    }

    //    Arrays.sort (files, fileComparator);

    for (File file : files)
    {
      String fileName = file.getName ().toLowerCase ();

      if (file.isDirectory ())
      {
        ++totalFolders;
        traverse (file);
      }
      else if (Utility.validFileType (fileName))
      {
        ++totalDisks;
        incrementType (file, fileName);
        checkDuplicates (file, fileName);
      }
    }
  }

  private void checkDuplicates (File file, String fileName)
  {
    String rootName = file.getAbsolutePath ().substring (rootFolderNameLength);
    DiskDetails diskDetails = new DiskDetails (file, rootName, fileName);

    if (fileNameMap.containsKey (fileName))
      fileNameMap.get (fileName).addDuplicateName (diskDetails);
    else
      fileNameMap.put (fileName, diskDetails);

    long checksum = diskDetails.getChecksum ();
    if (checksumMap.containsKey (checksum))
      checksumMap.get (checksum).addDuplicateChecksum (diskDetails);
    else
      checksumMap.put (checksum, diskDetails);
  }

  private void incrementType (File file, String fileName)
  {
    int pos = file.getName ().lastIndexOf ('.');
    if (pos > 0)
    {
      String type = fileName.substring (pos + 1);
      if (typeList.containsKey (type))
      {
        int t = typeList.get (type);
        typeList.put (type, ++t);
      }
      else
        typeList.put (type, 1);
    }
  }
}