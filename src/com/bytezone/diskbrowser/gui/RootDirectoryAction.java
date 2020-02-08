package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;
import com.bytezone.common.Platform;

// -----------------------------------------------------------------------------------//
class RootDirectoryAction extends DefaultAction implements QuitListener
// -----------------------------------------------------------------------------------//
{
  private static final String prefsRootDirectory = "Root directory";
  private final List<RootDirectoryChangeListener> listeners = new ArrayList<> ();
  private File rootFolder;

  // ---------------------------------------------------------------------------------//
  RootDirectoryAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Set HOME folder...", "Defines root folder where the disk images are kept",
        "/com/bytezone/diskbrowser/icons/");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt H"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_H);

    setIcon (Action.SMALL_ICON, "folder_explore_16.png");
    setIcon (Action.LARGE_ICON_KEY, "folder_explore_32.png");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    JFileChooser chooser = new JFileChooser (Platform.userHome);
    chooser.setDialogTitle ("Select FOLDER containing disk images");
    chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    if (rootFolder != null)
      chooser.setSelectedFile (rootFolder);

    int result = chooser.showDialog (null, "Accept");
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File rootDirectoryFile = chooser.getSelectedFile ();
      if (!rootDirectoryFile.isDirectory ())
        rootDirectoryFile = rootDirectoryFile.getParentFile ();
      if (rootDirectoryFile != null)
        notifyListeners (rootDirectoryFile);
    }
  }

  // ---------------------------------------------------------------------------------//
  public void addListener (RootDirectoryChangeListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void quit (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    prefs.put (prefsRootDirectory,
        rootFolder == null ? "" : rootFolder.getAbsolutePath ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    String rootDirectory = prefs.get (prefsRootDirectory, "");

    File rootDirectoryFile = new File (rootDirectory);

    if (!rootDirectoryFile.exists () || !rootDirectoryFile.isDirectory ())
    {
      System.out.println ("No root directory");
      return;
    }
    notifyListeners (rootDirectoryFile);
  }

  // ---------------------------------------------------------------------------------//
  private void notifyListeners (File newRootFolder)
  // ---------------------------------------------------------------------------------//
  {
    File oldRootFolder = rootFolder;
    rootFolder = newRootFolder;
    for (RootDirectoryChangeListener listener : listeners)
      listener.rootDirectoryChanged (oldRootFolder, newRootFolder);
  }
}