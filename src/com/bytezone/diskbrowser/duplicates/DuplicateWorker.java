package com.bytezone.diskbrowser.duplicates;

import java.util.List;

import javax.swing.SwingWorker;

public class DuplicateWorker extends SwingWorker<List<DiskDetails>, Void>
{
  List<DiskDetails> duplicateDisks;
  DuplicateWindow owner;

  public DuplicateWorker (List<DiskDetails> duplicateDisks, DuplicateWindow owner)
  {
    this.duplicateDisks = duplicateDisks;
    this.owner = owner;
  }

  @Override
  protected void done ()
  {
    try
    {
      owner.addResult (get ());
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }

  @Override
  protected List<DiskDetails> doInBackground () throws Exception
  {
    long firstChecksum = -1;
    for (DiskDetails dd : duplicateDisks)
    {
      if (firstChecksum < 0)
        firstChecksum = dd.getChecksum ();
      else
        dd.setDuplicate (dd.getChecksum () == firstChecksum);
    }
    return duplicateDisks;
  }
}