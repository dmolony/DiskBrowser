package com.bytezone.diskbrowser.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.duplicates.DuplicateWindow;
import com.bytezone.diskbrowser.duplicates.DuplicateWorker;
import com.bytezone.diskbrowser.gui.RootDirectoryAction.RootDirectoryChangeListener;

public class DuplicateAction extends DefaultAction implements RootDirectoryChangeListener
{
  int rootFolderLength;
  File rootFolder;
  DuplicateWindow window;

  public DuplicateAction ()
  {
    super ("Check for duplicates...", "Check for duplicate disks",
        "/com/bytezone/diskbrowser/icons/");

    setIcon (Action.SMALL_ICON, "save_delete_16.png");
    setIcon (Action.LARGE_ICON_KEY, "save_delete_32.png");
    int mask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask ();
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_D, mask));
    setEnabled (false);
  }

  @Override
  public void rootDirectoryChanged (File rootFolder)
  {
    this.rootFolder = rootFolder;
    setEnabled (rootFolder != null);
    window = null;
  }

  @Override
  public void actionPerformed (ActionEvent arg0)
  {
    if (window == null)
    {
      window = new DuplicateWindow (rootFolder);
      new DuplicateWorker (rootFolder, window).execute ();
    }
    else
      window.setVisible (true);
  }
}