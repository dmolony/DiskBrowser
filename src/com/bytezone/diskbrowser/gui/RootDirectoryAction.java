package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;
import com.bytezone.common.Platform;

class RootDirectoryAction extends DefaultAction
{
  File rootDirectory;
  CatalogPanel catalogPanel;

  public RootDirectoryAction (File rootDirectory, CatalogPanel catalogPanel)
  {
    super ("Set HOME folder...", "Defines root folder where the disk images are kept",
          "/com/bytezone/diskbrowser/icons/");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt H"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_H);
    this.rootDirectory = rootDirectory;
    this.catalogPanel = catalogPanel;

    setIcon (Action.SMALL_ICON, "folder_explore_16.png");
    setIcon (Action.LARGE_ICON_KEY, "folder_explore_32.png");
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    JFileChooser chooser = new JFileChooser (Platform.userHome);
    chooser.setDialogTitle ("Select FOLDER containing disk images");
    chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    if (rootDirectory != null)
      chooser.setSelectedFile (rootDirectory);
    int result = chooser.showDialog (null, "Accept");
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getSelectedFile ();
      if (!file.isDirectory ())
        file = file.getParentFile ();
      if (file != null)
      {
        rootDirectory = file;
        catalogPanel.changeRootPanel (file);
      }
    }
  }
}