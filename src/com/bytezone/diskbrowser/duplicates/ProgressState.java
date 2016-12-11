package com.bytezone.diskbrowser.duplicates;

import java.io.File;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

public class ProgressState
{
  List<String> suffixes = Utility.suffixes;
  int totalDisks;
  int totalFolders;

  // total files for each suffix (uncompressed, .gz, .zip)
  private final int[][] typeTotals = new int[3][suffixes.size ()];

  public ProgressState ()
  {

  }

  public void incrementFolders ()
  {
    ++totalFolders;
  }

  public void incrementType (File file, String filename)
  {
    int pos = Utility.getSuffixNo (filename);
    if (pos >= 0)
    {
      int cmp = 0;
      if (filename.endsWith (".gz"))
        cmp = 1;
      else if (filename.endsWith (".zip"))
        cmp = 2;
      typeTotals[cmp][pos]++;
      ++totalDisks;
    }
    else
      System.out.println ("no suffix: " + filename);
  }

  public void print ()
  {
    System.out.printf ("%nFolders ...... %,7d%n", totalFolders);
    System.out.printf ("Disks ........ %,7d%n%n", totalDisks);

    int grandTotal[] = new int[3];

    String line = "--------------  -------  -------  -------";
    System.out.println ("     type        uncmp      .gz     .zip");
    System.out.println (line);
    for (int i = 0; i < typeTotals[0].length; i++)
    {
      System.out.printf ("%14.14s  ", Utility.suffixes.get (i) + " ...........");
      for (int j = 0; j < typeTotals.length; j++)
      {
        System.out.printf ("%,7d  ", typeTotals[j][i]);
        grandTotal[j] += typeTotals[j][i];
      }
      System.out.println ();
    }

    System.out.println (line);
    System.out.printf ("Total           %,7d  %,7d  %,7d%n%n", grandTotal[0],
        grandTotal[1], grandTotal[2]);
  }
}