package com.bytezone.diskbrowser.duplicates;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import com.bytezone.diskbrowser.utilities.Utility;

public class DuplicateSwingWorker extends SwingWorker<Void, ProgressState>
{
  private final int rootFolderNameLength;
  private final RootFolderData rootFolderData;

  public DuplicateSwingWorker (RootFolderData rootFolderData)
  {
    this.rootFolderData = rootFolderData;
    rootFolderNameLength = rootFolderData.rootFolder.getAbsolutePath ().length ();

    rootFolderData.dialog.setLocationRelativeTo (null);
    rootFolderData.dialog.setVisible (true);
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
      String fileName = file.getName ().toLowerCase ();

      if (file.isDirectory ())
      {
        rootFolderData.progressState.incrementFolders ();
        traverse (file);
      }
      else if (Utility.validFileType (fileName) && file.length () > 0)
      {
        rootFolderData.progressState.incrementType (file, fileName);
        checkDuplicates (file, fileName);

        if ((rootFolderData.progressState.totalDisks % 500) == 0)
          publish (rootFolderData.progressState);
      }
    }
  }

  private void checkDuplicates (File file, String filename)
  {
    String rootName = file.getAbsolutePath ().substring (rootFolderNameLength);
    DiskDetails diskDetails =
        new DiskDetails (file, rootName, filename, rootFolderData.doChecksums);

    if (rootFolderData.fileNameMap.containsKey (filename))
      rootFolderData.fileNameMap.get (filename).addDuplicateName (diskDetails);
    else
      rootFolderData.fileNameMap.put (filename, diskDetails);

    if (rootFolderData.doChecksums)
    {
      long checksum = diskDetails.getChecksum ();
      if (rootFolderData.checksumMap.containsKey (checksum))
        rootFolderData.checksumMap.get (checksum).addDuplicateChecksum (diskDetails);
      else
        rootFolderData.checksumMap.put (checksum, diskDetails);
    }
  }

  @Override
  protected void done ()
  {
    try
    {
      if (!rootFolderData.showTotals)
        rootFolderData.dialog.setVisible (false);
      rootFolderData.window.setTableData (rootFolderData);
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }

  @Override
  protected Void doInBackground () throws Exception
  {
    traverse (rootFolderData.rootFolder);
    rootFolderData.progressState.print ();
    return null;
  }

  @Override
  protected void process (List<ProgressState> chunks)
  {
    rootFolderData.progressPanel.repaint ();
  }
}