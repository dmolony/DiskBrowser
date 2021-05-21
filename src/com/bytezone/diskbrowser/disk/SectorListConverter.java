package com.bytezone.diskbrowser.disk;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class SectorListConverter
// -----------------------------------------------------------------------------------//
{
  public final List<DiskAddress> sectors;
  public final String sectorText;

  // ---------------------------------------------------------------------------------//
  public SectorListConverter (String text, Disk disk)
  // ---------------------------------------------------------------------------------//
  {
    sectors = new ArrayList<> ();
    sectorText = text;

    for (String s : text.split (";"))
    {
      int pos = s.indexOf ('-');
      if (pos > 0)
      {
        int lo = Integer.parseInt (s.substring (0, pos));
        int hi = Integer.parseInt (s.substring (pos + 1));
        for (int i = lo; i <= hi; i++)
          sectors.add (disk.getDiskAddress (i));
      }
      else
        sectors.add (disk.getDiskAddress (Integer.parseInt (s)));
    }
  }

  // ---------------------------------------------------------------------------------//
  public SectorListConverter (List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    this.sectors = sectors;
    StringBuilder text = new StringBuilder ();

    int firstBlock = -2;
    int runLength = 0;

    for (DiskAddress da : sectors)
    {
      if (da.getBlockNo () == firstBlock + 1 + runLength)
      {
        ++runLength;
        continue;
      }

      if (firstBlock >= 0)
        addToText (text, firstBlock, runLength);

      firstBlock = da.getBlockNo ();
      runLength = 0;
    }
    addToText (text, firstBlock, runLength);
    sectorText = text.deleteCharAt (text.length () - 1).toString ();
  }

  // ---------------------------------------------------------------------------------//
  private void addToText (StringBuilder text, int firstBlock, int runLength)
  // ---------------------------------------------------------------------------------//
  {
    if (runLength == 0)
      text.append (firstBlock + ";");
    else
      text.append (firstBlock + "-" + (firstBlock + runLength) + ";");
  }
}