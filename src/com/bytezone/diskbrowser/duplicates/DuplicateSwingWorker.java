package com.bytezone.diskbrowser.duplicates;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.bytezone.diskbrowser.utilities.Utility;

public class DuplicateSwingWorker extends SwingWorker<Void, ProgressState>
{
  private final File rootFolder;
  private final int rootFolderNameLength;
  //  private final ProgressState progressState = new ProgressState ();
  private final DuplicateWindow owner;
  private final JDialog dialog;
  private final ProgressPanel progressPanel;
  private final boolean doChecksums;
  private final RootFolderData rootFolderData;

  public DuplicateSwingWorker (File rootFolder, DuplicateWindow owner,
      boolean doChecksums)
  {
    this.rootFolder = rootFolder;
    this.owner = owner;
    this.doChecksums = doChecksums;
    rootFolderNameLength = rootFolder.getAbsolutePath ().length ();

    rootFolderData = new RootFolderData ();

    dialog = new JDialog (owner);
    progressPanel = new ProgressPanel ();
    progressPanel.setPreferredSize (new Dimension (485, 300));
    dialog.add (progressPanel);
    dialog.setTitle ("Reading disks");
    dialog.pack ();
    dialog.setLocationRelativeTo (null);
    dialog.setVisible (true);
  }

  File getRootFolder ()
  {
    return rootFolder;
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
    DiskDetails diskDetails = new DiskDetails (file, rootName, filename, doChecksums);

    if (rootFolderData.fileNameMap.containsKey (filename))
      rootFolderData.fileNameMap.get (filename).addDuplicateName (diskDetails);
    else
      rootFolderData.fileNameMap.put (filename, diskDetails);

    if (doChecksums)
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
      dialog.setVisible (false);
      owner.setTableData (rootFolderData);
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }

  @Override
  protected Void doInBackground () throws Exception
  {
    traverse (rootFolder);
    rootFolderData.progressState.print ();
    return null;
  }

  @Override
  protected void process (List<ProgressState> chunks)
  {
    progressPanel.repaint ();
  }

  class ProgressPanel extends JPanel
  {
    @Override
    protected void paintComponent (Graphics graphics)
    {
      super.paintComponent (graphics);
      rootFolderData.progressState.paintComponent (graphics);
    }
  }
}