package com.bytezone.diskbrowser.gui;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.disk.AppleDiskAddress;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;

// -----------------------------------------------------------------------------------//
class DiskLayoutSelection implements Iterable<DiskAddress>
// -----------------------------------------------------------------------------------//
{
  private final List<DiskAddress> highlights;

  public DiskLayoutSelection ()
  {
    highlights = new ArrayList<> ();
  }

  // ---------------------------------------------------------------------------------//
  public void doClick (Disk disk, DiskAddress da, boolean extend, boolean append)
  // ---------------------------------------------------------------------------------//
  {
    /*
     * Single click without modifiers - just replace previous highlights with the new
     * sector. If there are no current highlights then even modifiers have the same
     * effect.
     */
    if ((!extend && !append) || highlights.size () == 0)
    {
      highlights.clear ();
      addHighlight (da);
      return;
    }

    /*
     * If the click was on an existing highlight, just remove it (regardless of modifiers)
     */
    for (DiskAddress setDA : highlights)
      if (da.matches (setDA))
      {
        highlights.remove (setDA);
        return;
      }

    /*
     * Appending - just add the sector to the existing highlights
     */
    if (append)
    {
      addHighlight (da);
      Collections.sort (highlights);
      return;
    }

    /*
     * Extending - if the existing selection is contiguous then just extend it. If not
     * then things get a bit trickier.
     */
    if (checkContiguous ())
      extendHighlights (disk, da);
    else
      adjustHighlights (disk, da);

    Collections.sort (highlights);
  }

  // ---------------------------------------------------------------------------------//
  void cursorMove (FormattedDisk formattedDisk, KeyEvent e)
  // ---------------------------------------------------------------------------------//
  {
    if (highlights.size () == 0)
    {
      System.out.println ("Nothing to move");
      return;
    }

    Disk disk = formattedDisk.getDisk ();

    DiskAddress first = highlights.get (0);
    DiskAddress last = highlights.get (highlights.size () - 1);

    if (!e.isShiftDown ())
      highlights.clear ();

    int totalBlocks = disk.getTotalBlocks ();
    //    int rowSize = disk.getTrackSize () / disk.getBlockSize ();
    Dimension gridLayout = formattedDisk.getGridLayout ();
    int rowSize = gridLayout.width;

    switch (e.getKeyCode ())
    {
      case KeyEvent.VK_LEFT:
        int block = first.getBlockNo () - 1;
        if (block < 0)
          block = totalBlocks - 1;
        addHighlight (disk.getDiskAddress (block));
        break;

      case KeyEvent.VK_RIGHT:
        block = last.getBlockNo () + 1;
        if (block >= totalBlocks)
          block = 0;
        addHighlight (disk.getDiskAddress (block));
        break;

      case KeyEvent.VK_UP:
        block = first.getBlockNo () - rowSize;
        if (block < 0)
          block += totalBlocks;
        addHighlight (disk.getDiskAddress (block));
        break;

      case KeyEvent.VK_DOWN:
        block = last.getBlockNo () + rowSize;
        if (block >= totalBlocks)
          block -= totalBlocks;
        addHighlight (disk.getDiskAddress (block));
        break;
    }
    Collections.sort (highlights);
  }

  // ---------------------------------------------------------------------------------//
  private void addHighlight (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    assert da != null;
    highlights.add (da);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<DiskAddress> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return highlights.iterator ();
  }

  // This must return a copy, or the redo function will get very confused
  // ---------------------------------------------------------------------------------//
  public List<DiskAddress> getHighlights ()
  // ---------------------------------------------------------------------------------//
  {
    return new ArrayList<> (highlights);
  }

  // ---------------------------------------------------------------------------------//
  public boolean isSelected (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    for (DiskAddress selection : highlights)
      if (selection != null && da.matches (selection))
        return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  public void setSelection (List<DiskAddress> list)
  // ---------------------------------------------------------------------------------//
  {
    // sparse files contain empty blocks
    highlights.clear ();
    if (list != null)
      for (DiskAddress da : list)
        if (da != null && (da.getBlockNo () > 0 || ((AppleDiskAddress) da).zeroFlag ()))
          highlights.add (da);
  }

  // ---------------------------------------------------------------------------------//
  private boolean checkContiguous ()
  // ---------------------------------------------------------------------------------//
  {
    int range = highlights.get (highlights.size () - 1).getBlockNo ()
        - highlights.get (0).getBlockNo () + 1;
    return (range == highlights.size ());
  }

  // ---------------------------------------------------------------------------------//
  private void extendHighlights (Disk disk, DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    int lo, hi;

    // Are we extending in front of the current block?
    if (highlights.get (0).getBlockNo () > da.getBlockNo ())
    {
      lo = da.getBlockNo ();
      hi = highlights.get (0).getBlockNo () - 1;
    }
    else
    // No, must be extending at the end
    {
      lo = highlights.get (highlights.size () - 1).getBlockNo () + 1;
      hi = da.getBlockNo ();
    }

    for (int i = lo; i <= hi; i++)
      addHighlight (disk.getDiskAddress (i));
  }

  // ---------------------------------------------------------------------------------//
  private void adjustHighlights (Disk disk, DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    // If we are outside the discontiguous range, just extend as usual
    if (da.getBlockNo () < highlights.get (0).getBlockNo ()
        || da.getBlockNo () > highlights.get (highlights.size () - 1).getBlockNo ())
    {
      extendHighlights (disk, da);
      return;
    }

    // just treat it like a ctrl-click (hack!!)
    addHighlight (da);
  }
}