package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;
import com.bytezone.common.Platform;

class RootDirectoryAction extends DefaultAction
{
  private File rootDirectory;
  private final List<RootDirectoryChangeListener> listeners =
      new ArrayList<RootDirectoryAction.RootDirectoryChangeListener> ();

  public RootDirectoryAction (File rootDirectory)
  {
    super ("Set HOME folder...", "Defines root folder where the disk images are kept",
        "/com/bytezone/diskbrowser/icons/");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt H"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_H);

    setIcon (Action.SMALL_ICON, "folder_explore_16.png");
    setIcon (Action.LARGE_ICON_KEY, "folder_explore_32.png");

    this.rootDirectory = rootDirectory;
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
        for (RootDirectoryChangeListener listener : listeners)
          listener.rootDirectoryChanged (file);
      }
    }
  }

  public void addListener (RootDirectoryChangeListener listener)
  {
    if (!listeners.contains (listener))
    {
      listeners.add (listener);
      listener.rootDirectoryChanged (rootDirectory);
    }
  }

  interface RootDirectoryChangeListener
  {
    public void rootDirectoryChanged (File newRootDirectory);
  }
}