package com.bytezone.diskbrowser.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.duplicates.DiskDetails;
import com.bytezone.diskbrowser.duplicates.RootFolderData;

public class DuplicateAction extends DefaultAction implements RootDirectoryChangeListener
{
  RootFolderData rootFolderData;

  public DuplicateAction (RootFolderData rootFolderData)
  {
    super ("List disks...", "Display a sortable list of disks",
        "/com/bytezone/diskbrowser/icons/");

    this.rootFolderData = rootFolderData;

    setIcon (Action.SMALL_ICON, "save_delete_16.png");
    setIcon (Action.LARGE_ICON_KEY, "save_delete_32.png");
    int mask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMaskEx ();
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_L, mask));
    setEnabled (rootFolderData.getRootFolder () != null);
  }

  @Override
  public void rootDirectoryChanged (File oldRootFolder, File newRootFolder)
  {
    assert rootFolderData.getRootFolder () == newRootFolder;
    setEnabled (rootFolderData.getRootFolder () != null);
  }

  @Override
  public void actionPerformed (ActionEvent arg0)
  {
    if (rootFolderData.disksWindow == null)
    {
      Object[] options = { "Generate checksums", "Disk names only", "Cancel" };
      int option = JOptionPane.showOptionDialog (null,
          "This command will list all of the disks in the root folder (including\n"
              + "nested folders). If you wish to generate a checksum for each disk, it\n"
              + "may slow the process down considerably.\n\n"
              + "Do you wish to generate checksums?",
          "Generate Disk Listing", JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE, null, options, options[1]);   // just disk names
      if (option < 2)
        rootFolderData.count (option == 0);
    }
    else
      rootFolderData.disksWindow.setVisible (true);
  }

  public void addTableSelectionListener (DiskTableSelectionListener listener)
  {
    if (!rootFolderData.listeners.contains (listener))
      rootFolderData.listeners.add (listener);
  }

  public interface DiskTableSelectionListener
  {
    public void diskSelected (DiskDetails diskDetails);
  }
}