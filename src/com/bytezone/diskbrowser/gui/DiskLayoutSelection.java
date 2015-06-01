package com.bytezone.diskbrowser.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;

class DiskLayoutSelection implements Iterable<DiskAddress>
{
  private final List<DiskAddress> highlights;

  public DiskLayoutSelection ()
  {
    highlights = new ArrayList<DiskAddress> ();
  }

  public void doClick (Disk disk, DiskAddress da, boolean extend, boolean append)
  {
    /*
     * Single click without modifiers - just replace previous highlights with the new
     * sector. If there are no current highlights then even modifiers have the same
     * effect.
     */
    if ((!extend && !append) || highlights.size () == 0)
    {
      highlights.clear ();
      highlights.add (da);
      return;
    }

    /*
     * If the click was on an existing highlight, just remove it (regardless of modifiers)
     */
    for (DiskAddress setDA : highlights)
      if (da.compareTo (setDA) == 0)
      {
        highlights.remove (setDA);
        return;
      }

    /*
     * Appending - just add the sector to the existing highlights
     */
    if (append)
    {
      highlights.add (da);
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

  public Iterator<DiskAddress> iterator ()
  {
    return highlights.iterator ();
  }

  // This must return a copy, or the redo function will get very confused
  public List<DiskAddress> getHighlights ()
  {
    return new ArrayList<DiskAddress> (highlights);
  }

  public void setSelection (List<DiskAddress> list)
  {
    highlights.clear ();
    if (list != null)
      highlights.addAll (list);
  }

  private boolean checkContiguous ()
  {
    int range =
          highlights.get (highlights.size () - 1).getBlock () - highlights.get (0).getBlock () + 1;
    return (range == highlights.size ());
  }

  private void extendHighlights (Disk disk, DiskAddress da)
  {
    int lo, hi;

    // Are we extending in front of the current block?
    if (highlights.get (0).getBlock () > da.getBlock ())
    {
      lo = da.getBlock ();
      hi = highlights.get (0).getBlock () - 1;
    }
    else
    // No, must be extending at the end
    {
      lo = highlights.get (highlights.size () - 1).getBlock () + 1;
      hi = da.getBlock ();
    }

    for (int i = lo; i <= hi; i++)
      highlights.add (disk.getDiskAddress (i));
  }

  private void adjustHighlights (Disk disk, DiskAddress da)
  {
    // If we are outside the discontiguous range, just extend as usual
    if (da.getBlock () < highlights.get (0).getBlock ()
          || da.getBlock () > highlights.get (highlights.size () - 1).getBlock ())
    {
      extendHighlights (disk, da);
      return;
    }

    // just treat it like a ctrl-click (hack!!)
    highlights.add (da);
  }
}