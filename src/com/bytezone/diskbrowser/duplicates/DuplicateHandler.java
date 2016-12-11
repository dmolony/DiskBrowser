package com.bytezone.diskbrowser.duplicates;

import java.awt.Graphics;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.bytezone.diskbrowser.utilities.Utility;

public class DuplicateHandler extends SwingWorker<Void, ProgressState>
{
  private final File rootFolder;
  private final int rootFolderNameLength;
  private final ProgressState progressState = new ProgressState ();
  private final DuplicateWindow owner;
  private final JDialog dialog;
  private final ProgressPanel progressPanel;

  // list of checksum -> DiskDetails
  private final Map<Long, DiskDetails> checksumMap = new HashMap<Long, DiskDetails> ();

  // list of unique disk names -> DiskDetails
  private final Map<String, DiskDetails> fileNameMap =
      new TreeMap<String, DiskDetails> ();

  public DuplicateHandler (File rootFolder, DuplicateWindow owner)
  {
    this.rootFolder = rootFolder;
    this.owner = owner;
    rootFolderNameLength = rootFolder.getAbsolutePath ().length ();

    dialog = new JDialog (owner);
    progressPanel = new ProgressPanel ();
    dialog.add (progressPanel);
    dialog.setSize (500, 400);
    dialog.setLocationRelativeTo (null);
    dialog.setTitle ("Reading disks");
    dialog.setVisible (true);
  }

  public Map<String, DiskDetails> getFileNameMap ()
  {
    return fileNameMap;
  }

  public Map<Long, DiskDetails> getChecksumMap ()
  {
    return checksumMap;
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
        progressState.incrementFolders ();
        traverse (file);
      }
      else if (Utility.validFileType (fileName) && file.length () > 0)
      {
        progressState.incrementType (file, fileName);
        checkDuplicates (file, fileName);

        if ((progressState.totalDisks % 1000) == 0)
          publish (progressState);
      }
    }
  }

  private void checkDuplicates (File file, String filename)
  {
    String rootName = file.getAbsolutePath ().substring (rootFolderNameLength);
    DiskDetails diskDetails = new DiskDetails (file, rootName, filename);

    if (fileNameMap.containsKey (filename))
      fileNameMap.get (filename).addDuplicateName (diskDetails);
    else
      fileNameMap.put (filename, diskDetails);

    long checksum = diskDetails.getChecksum ();
    if (checksumMap.containsKey (checksum))
      checksumMap.get (checksum).addDuplicateChecksum (diskDetails);
    else
      checksumMap.put (checksum, diskDetails);
  }

  @Override
  protected void done ()
  {
    try
    {
      dialog.setVisible (false);
      owner.setDuplicateHandler (this);
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
    progressState.print ();
    return null;
  }

  @Override
  protected void process (List<ProgressState> chunks)
  {
    //      for (ProgressState progressState : chunks)
    //        progressState.print ();
    progressPanel.repaint ();
  }

  class ProgressPanel extends JPanel
  {
    @Override
    protected void paintComponent (Graphics graphics)
    {
      super.paintComponent (graphics);
      progressState.paintComponent (graphics);
    }
  }
}