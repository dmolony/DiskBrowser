package com.bytezone.diskbrowser.duplicates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

public class ProgressState
{
  private static final String header = "      type        uncmp      .gz     .zip";
  private static final String line = "--------------  -------  -------  -------";
  private static final List<String> suffixes = Utility.suffixes;
  private static final Font font = new Font ("Monospaced", Font.BOLD, 15);

  int totalDisks;
  int totalFolders;

  // total files for each suffix (uncompressed, .gz, .zip)
  private final int[][] typeTotals = new int[3][suffixes.size ()];

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

  void paintComponent (Graphics graphics)
  {
    Graphics2D g = (Graphics2D) graphics;

    g.setColor (Color.BLACK);
    g.setFont (font);

    int x = 55;
    int y = 25;
    int lineHeight = 23;
    String line;

    g.drawString (header, x, y);
    y += lineHeight + 10;

    int grandTotal[] = new int[3];

    for (int i = 0; i < typeTotals[0].length; i++)
    {
      line = String.format ("%14.14s  %,7d  %,7d  %,7d",
          Utility.suffixes.get (i) + " ...........", typeTotals[0][i], typeTotals[1][i],
          typeTotals[2][i]);
      g.drawString (line, x, y);
      for (int j = 0; j < typeTotals.length; j++)
        grandTotal[j] += typeTotals[j][i];

      y += lineHeight;
    }

    line = String.format ("Total           %,7d  %,7d  %,7d%n%n", grandTotal[0],
        grandTotal[1], grandTotal[2]);
    y += 10;
    g.drawString (line, x, y);
  }

  public void print ()
  {
    System.out.printf ("%nFolders ...... %,7d%n", totalFolders);
    System.out.printf ("Disks ........ %,7d%n%n", totalDisks);

    int grandTotal[] = new int[3];

    System.out.println (header);
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