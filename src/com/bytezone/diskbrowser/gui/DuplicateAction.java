package com.bytezone.diskbrowser.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.duplicates.DiskDetails;
import com.bytezone.diskbrowser.duplicates.DuplicateHandler;
import com.bytezone.diskbrowser.duplicates.DuplicateWindow;
import com.bytezone.diskbrowser.gui.RootDirectoryAction.RootDirectoryChangeListener;

public class DuplicateAction extends DefaultAction implements RootDirectoryChangeListener
{
  private File rootFolder;
  private DuplicateWindow window;
  private final List<DiskTableSelectionListener> listeners =
      new ArrayList<DiskTableSelectionListener> ();

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
      window = new DuplicateWindow (rootFolder, listeners);
      DuplicateHandler duplicateHandler = new DuplicateHandler (rootFolder, window);
      duplicateHandler.execute ();
    }
    else
      window.setVisible (true);
  }

  public void addTableSelectionListener (DiskTableSelectionListener listener)
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  public interface DiskTableSelectionListener
  {
    public void diskSelected (DiskDetails diskDetails);
  }
}