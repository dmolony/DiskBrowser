package com.bytezone.diskbrowser.duplicates;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class DuplicateSwingWorker extends SwingWorker<Void, RootFolderData>
// -----------------------------------------------------------------------------------//
{
  private final RootFolderData rootFolderData;

  // ---------------------------------------------------------------------------------//
  DuplicateSwingWorker (RootFolderData rootFolderData)
  // ---------------------------------------------------------------------------------//
  {
    this.rootFolderData = rootFolderData;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected Void doInBackground () throws Exception
  // ---------------------------------------------------------------------------------//
  {
    traverse (rootFolderData.getRootFolder ());
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected void done ()
  // ---------------------------------------------------------------------------------//
  {
    rootFolderData.done ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected void process (List<RootFolderData> chunks)
  // ---------------------------------------------------------------------------------//
  {
    rootFolderData.progressPanel.repaint ();
  }

  // ---------------------------------------------------------------------------------//
  private void traverse (File directory)
  // ---------------------------------------------------------------------------------//
  {
    if (rootFolderData.progressPanel.cancelled)
      return;

    File[] files = directory.listFiles ();

    if (files == null || files.length == 0)
    {
      //      System.out.println ("Empty folder : " + directory.getAbsolutePath ());
      return;
    }

    for (File file : files)
    {
      if (rootFolderData.progressPanel.cancelled)
        return;

      if (file.isHidden ())
        continue;

      if (file.isDirectory ())
      {
        if (file.getName ().equalsIgnoreCase ("emulators"))
          System.out.println ("ignoring: " + file.getAbsolutePath ());
        else
        {
          rootFolderData.incrementFolders ();
          traverse (file);
        }
      }
      else
      {
        String fileName = file.getName ().toLowerCase ();
        if (Utility.validFileType (fileName) && file.length () > 0)
        {
          rootFolderData.incrementType (file, fileName);
          if ((rootFolderData.totalDisks % 250) == 0)
            publish (rootFolderData);
        }
      }
    }
  }
}