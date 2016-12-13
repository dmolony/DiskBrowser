package com.bytezone.diskbrowser.duplicates;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.bytezone.diskbrowser.gui.DuplicateAction.DiskTableSelectionListener;
import com.bytezone.diskbrowser.utilities.Utility;

public class RootFolderData
{
  private File rootFolder;

  final Map<Long, DiskDetails> checksumMap = new HashMap<Long, DiskDetails> ();
  final Map<String, DiskDetails> fileNameMap = new TreeMap<String, DiskDetails> ();

  final ProgressPanel progressPanel;
  public JDialog dialog;
  public DuplicateWindow window;

  public final List<DiskTableSelectionListener> listeners =
      new ArrayList<DiskTableSelectionListener> ();

  public boolean doChecksums;
  public boolean showTotals;

  private static final String header =
      "      type        uncmp      .gz     .zip    total";
  private static final String line = "--------------  -------  -------  -------  -------";
  private static final Font font = new Font ("Monospaced", Font.BOLD, 15);

  int totalDisks;
  int totalFolders;

  // total files for each suffix (uncompressed, .gz, .zip, total)
  int[][] typeTotals;

  public RootFolderData ()
  {
    progressPanel = new ProgressPanel ();
    progressPanel.setPreferredSize (new Dimension (560, 300));

    dialog = new JDialog (window);
    dialog.add (progressPanel);
    dialog.setTitle ("Disk Totals");
    dialog.pack ();
  }

  public void setRootFolder (File rootFolder)
  {
    this.rootFolder = rootFolder;
    typeTotals = new int[4][Utility.suffixes.size ()];
    totalDisks = 0;
    totalFolders = 0;
    window = null;

    checksumMap.clear ();
    fileNameMap.clear ();
  }

  public File getRootFolder ()
  {
    return rootFolder;
  }

  public void incrementFolders ()
  {
    ++totalFolders;
  }

  public int getTotalType (int type)
  {
    return typeTotals[0][type] + typeTotals[1][type] + typeTotals[2][type];
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
      typeTotals[3][pos]++;
      ++totalDisks;
    }
    else
      System.out.println ("no suffix: " + filename);
  }

  public void print ()
  {
    System.out.printf ("%nFolders ...... %,7d%n", totalFolders);
    System.out.printf ("Disks ........ %,7d%n%n", totalDisks);

    int grandTotal[] = new int[4];

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
    System.out.printf ("Total           %,7d  %,7d  %,7d  %,7d%n%n", grandTotal[0],
        grandTotal[1], grandTotal[2], grandTotal[3]);
    System.out.printf ("Unique checksums: %,d%n", checksumMap.size ());
  }

  class ProgressPanel extends JPanel
  {
    @Override
    protected void paintComponent (Graphics graphics)
    {
      super.paintComponent (graphics);

      Graphics2D g = (Graphics2D) graphics;
      g.setColor (Color.BLACK);
      g.setFont (font);

      int x = 55;
      int y = 25;
      int lineHeight = 23;
      String line;

      g.drawString (header, x, y);
      y += lineHeight + 10;

      int grandTotal[] = new int[4];

      for (int i = 0; i < typeTotals[0].length; i++)
      {
        line = String.format ("%14.14s  %,7d  %,7d  %,7d  %,7d",
            Utility.suffixes.get (i) + " ...........", typeTotals[0][i], typeTotals[1][i],
            typeTotals[2][i], typeTotals[3][i]);
        g.drawString (line, x, y);
        for (int j = 0; j < typeTotals.length; j++)
          grandTotal[j] += typeTotals[j][i];

        y += lineHeight;
      }

      line = String.format ("Total           %,7d  %,7d  %,7d  %,7d%n%n", grandTotal[0],
          grandTotal[1], grandTotal[2], grandTotal[3]);
      y += 10;
      g.drawString (line, x, y);
    }
  }
}