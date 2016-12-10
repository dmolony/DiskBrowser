package com.bytezone.diskbrowser.gui;

import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.TreeBuilder.FileNode;

class DiskAndFileSelector
{
  EventListenerList listenerList = new EventListenerList ();
  FormattedDisk currentDisk;
  boolean redo;

  /*
   * Apple DiskSelection routines
   */
  public void addDiskSelectionListener (DiskSelectionListener listener)
  {
    listenerList.add (DiskSelectionListener.class, listener);
  }

  public void removeDiskSelectionListener (DiskSelectionListener listener)
  {
    listenerList.remove (DiskSelectionListener.class, listener);
  }

  public void addFileNodeSelectionListener (FileNodeSelectionListener listener)
  {
    listenerList.add (FileNodeSelectionListener.class, listener);
  }

  public void removeFileNodeSelectionListener (FileNodeSelectionListener listener)
  {
    listenerList.remove (FileNodeSelectionListener.class, listener);
  }

  //  public void fireDiskSelectionEvent (File file)
  //  {
  //    if (file.isDirectory ())
  //    {
  //      System.out.println ("Directory received : " + file.getAbsolutePath ());
  //      return;
  //    }
  //
  //    if (currentDisk != null) // will this screw up the refresh command?
  //    {
  //      System.out.println (currentDisk.getDisk ().getFile ().getAbsolutePath ());
  //      System.out.println (file.getAbsolutePath ());
  //    }
  //    if (currentDisk != null
  //          && currentDisk.getDisk ().getFile ().getAbsolutePath ().equals (file.getAbsolutePath ()))
  //      fireDiskSelectionEvent (currentDisk);
  //    else
  //    {
  //      System.out.println ("  creating disk from a file");
  //      fireDiskSelectionEvent (DiskFactory.createDisk (file.getAbsolutePath ()));
  //    }
  //  }

  void fireDiskSelectionEvent (FileNode node)
  {
    if (node.file.isDirectory ())
    {
      fireFileNodeSelectionEvent (node);
      currentDisk = null;
    }
    else
    {
      FormattedDisk fd = node.getFormattedDisk ();
      if (fd == null)
        JOptionPane.showMessageDialog (null, "Incorrect file format", "Format error",
            JOptionPane.ERROR_MESSAGE);
      else
        fireDiskSelectionEvent (fd);
    }
  }

  void fireFileNodeSelectionEvent (FileNode node)
  {
    FileNodeSelectedEvent e = new FileNodeSelectedEvent (this, node);
    e.redo = redo;
    FileNodeSelectionListener[] listeners =
        (listenerList.getListeners (FileNodeSelectionListener.class));
    for (FileNodeSelectionListener listener : listeners)
      listener.fileNodeSelected (e);
  }

  void fireDiskSelectionEvent (FormattedDisk disk)
  {
    if (disk == currentDisk)
    {
      //      System.out.println ("Disk event duplicated");
      return;
    }

    if (disk == null)
    {
      System.out.println ("Null disk in fireDiskSelectionEvent()");
      return;
    }

    DiskSelectedEvent e = new DiskSelectedEvent (this, disk);
    e.redo = redo;
    DiskSelectionListener[] listeners =
        (listenerList.getListeners (DiskSelectionListener.class));
    for (DiskSelectionListener listener : listeners)
      listener.diskSelected (e);
    currentDisk = disk;
  }

  /*
   * Apple FileSelection routines
   */

  public void addFileSelectionListener (FileSelectionListener listener)
  {
    listenerList.add (FileSelectionListener.class, listener);
  }

  public void removeFileSelectionListener (FileSelectionListener listener)
  {
    listenerList.remove (FileSelectionListener.class, listener);
  }

  void fireFileSelectionEvent (AppleFileSource file)
  {
    assert file != null;
    currentDisk = null;
    FileSelectedEvent e = new FileSelectedEvent (this, file);
    e.redo = redo;
    FileSelectionListener[] listeners =
        (listenerList.getListeners (FileSelectionListener.class));
    for (FileSelectionListener listener : listeners)
      listener.fileSelected (e);
  }
}