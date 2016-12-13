package com.bytezone.diskbrowser.duplicates;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import com.bytezone.diskbrowser.utilities.Utility;

public class DuplicateSwingWorker extends SwingWorker<Void, RootFolderData>
{
  private final RootFolderData rootFolderData;

  public DuplicateSwingWorker (RootFolderData rootFolderData)
  {
    this.rootFolderData = rootFolderData;
    rootFolderData.dialogTotals.setVisible (true);
  }

  @Override
  protected Void doInBackground () throws Exception
  {
    traverse (rootFolderData.getRootFolder ());
    publish (rootFolderData);
    rootFolderData.print ();
    return null;
  }

  @Override
  protected void done ()
  {
    try
    {
      if (!rootFolderData.showTotals)
        rootFolderData.dialogTotals.setVisible (false);
      rootFolderData.windowDisks.setTableData (rootFolderData);
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }

  @Override
  protected void process (List<RootFolderData> chunks)
  {
    rootFolderData.progressPanel.repaint ();
  }

  private void traverse (File directory)
  {
    File[] files = directory.listFiles ();

    if (files == null || files.length == 0)
    {
      System.out.println ("Empty folder : " + directory.getAbsolutePath ());
      return;
    }

    for (File file : files)
    {
      if (file.isDirectory ())
      {
        rootFolderData.incrementFolders ();
        traverse (file);
      }
      else
      {
        String fileName = file.getName ().toLowerCase ();
        if (Utility.validFileType (fileName) && file.length () > 0)
        {
          rootFolderData.incrementType (file, fileName);
          if ((rootFolderData.totalDisks % 500) == 0)
            publish (rootFolderData);
        }
      }
    }
  }
}