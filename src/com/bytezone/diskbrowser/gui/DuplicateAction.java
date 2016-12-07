package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.duplicates.DiskDetails;
import com.bytezone.diskbrowser.duplicates.DuplicateWindow;
import com.bytezone.diskbrowser.duplicates.DuplicateWorker;
import com.bytezone.diskbrowser.gui.RootDirectoryAction.RootDirectoryListener;

public class DuplicateAction extends DefaultAction implements RootDirectoryListener
{
  Map<String, List<DiskDetails>> duplicateDisks;
  int rootFolderLength;
  File rootFolder;
  DuplicateWindow window;

  public DuplicateAction ()
  {
    super ("Check for duplicates...", "Check for duplicate disks",
        "/com/bytezone/diskbrowser/icons/");

    setIcon (Action.SMALL_ICON, "save_delete_16.png");
    setIcon (Action.LARGE_ICON_KEY, "save_delete_32.png");
  }

  //  public void setDuplicates (File rootFolder,
  //      Map<String, List<DiskDetails>> duplicateDisks)
  //  {
  //    this.duplicateDisks = duplicateDisks;
  //    this.rootFolderLength = rootFolder.getAbsolutePath ().length ();
  //    setEnabled (duplicateDisks.size () > 0);
  //  }

  @Override
  public void rootDirectoryChanged (File newRootDirectory)
  {
    this.rootFolder = newRootDirectory;
    System.out.println ("gotcha");
  }

  @Override
  public void actionPerformed (ActionEvent arg0)
  {
    if (duplicateDisks == null)
    {
      System.out.println ("No duplicate disks found");
      return;
    }

    if (window != null)
    {
      window.setVisible (true);
      return;
    }
    window = new DuplicateWindow (rootFolder);
    for (List<DiskDetails> diskList : duplicateDisks.values ())
      new DuplicateWorker (diskList, window).execute ();
  }
}