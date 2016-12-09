package com.bytezone.diskbrowser.duplicates;

import java.io.File;

import javax.swing.SwingWorker;

public class DuplicateWorker extends SwingWorker<DuplicateHandler, String>
{
  DuplicateHandler duplicateHandler;
  DuplicateWindow owner;

  public DuplicateWorker (File rootFolder, DuplicateWindow owner)
  {
    this.owner = owner;
    duplicateHandler = new DuplicateHandler (rootFolder);
  }

  @Override
  protected void done ()
  {
    try
    {
      owner.setDuplicateHandler (get ());
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }

  @Override
  protected DuplicateHandler doInBackground () throws Exception
  {
    duplicateHandler.countDisks ();

    return duplicateHandler;
  }
}