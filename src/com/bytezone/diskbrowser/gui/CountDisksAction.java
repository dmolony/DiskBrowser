package com.bytezone.diskbrowser.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.duplicates.DuplicateSwingWorker;
import com.bytezone.diskbrowser.duplicates.DuplicateWindow;
import com.bytezone.diskbrowser.duplicates.RootFolderData;
import com.bytezone.diskbrowser.gui.RootDirectoryAction.RootDirectoryChangeListener;

public class CountDisksAction extends DefaultAction implements RootDirectoryChangeListener
{
  RootFolderData rootFolderData;

  public CountDisksAction (RootFolderData rootFolderData)
  {
    super ("Count disks...", "Display a window showing disk totals");

    this.rootFolderData = rootFolderData;

    int mask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask ();
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_I, mask));
    setEnabled (rootFolderData.getRootFolder () != null);
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    if (rootFolderData.window == null)
    {
      rootFolderData.showTotals = true;
      rootFolderData.window = new DuplicateWindow (rootFolderData);
      new DuplicateSwingWorker (rootFolderData).execute ();
    }
    else
      rootFolderData.dialog.setVisible (true);
  }

  @Override
  public void rootDirectoryChanged (File rootFolder)
  {
    rootFolderData.setRootFolder (rootFolder);
    setEnabled (rootFolder != null);
  }
}