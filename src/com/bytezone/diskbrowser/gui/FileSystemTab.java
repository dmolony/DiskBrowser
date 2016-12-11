package com.bytezone.diskbrowser.gui;

/***********************************************************************************************
 * JPanel which displays a scrolling JTree containing details of all disks in the user's
 * root directory. The JTree consists entirely of FileNode objects (which are simply
 * wrappers for File objects). There will always be exactly one instance contained in
 * Catalog Panel, along with any number of AppleDiskTab instances.
 ***********************************************************************************************/

import java.awt.Font;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import com.bytezone.diskbrowser.disk.DiskFactory;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.RedoHandler.RedoEvent;
import com.bytezone.diskbrowser.gui.TreeBuilder.FileNode;

class FileSystemTab extends AbstractTab
{
  File rootFolder;

  public FileSystemTab (File folder, DiskAndFileSelector selector, RedoHandler navMan,
      Font font, DiskSelectedEvent diskEvent)
  {
    super (navMan, selector, font);
    this.rootFolder = folder;

    TreeBuilder tb = new TreeBuilder (folder);

    setTree (tb.getTree ());
    setSelectionListener (tree);

    if (diskEvent == null)
    {
      DefaultMutableTreeNode node = findFirstLeafNode ();
      if (node != null)
      {
        FileNode fn = (FileNode) node.getUserObject ();
        diskEvent = new DiskSelectedEvent (this, DiskFactory.createDisk (fn.file));
      }
    }

    if (diskEvent != null)
      navMan.diskSelected (diskEvent);
    else
      System.out.println ("No disk event");

    // temporary code while I sort out the DOS checksum feature
    //    if (tb.dosMap.keySet ().size () > 0)
    //    {
    //      System.out.printf ("Unique DOSs : %4d%n", tb.dosMap.keySet ().size ());
    //      long lastKey = -1;
    //      int beginIndex = rootFolder.getAbsolutePath ().length ();
    //      for (Long key : tb.dosMap.keySet ())
    //      {
    //        if (key != lastKey)
    //        {
    //          lastKey = key;
    //          System.out.printf ("%,14d  (%d)%n", key, tb.dosMap.get (key).size ());
    //        }
    //        for (File file : tb.dosMap.get (key))
    //          System.out.printf ("                      %s%n",
    //              file.getAbsolutePath ().substring (beginIndex));
    //      }
    //    }
  }

  public FileSystemTab (File folder, DiskAndFileSelector selector, RedoHandler navMan,
      Font font)
  {
    this (folder, selector, navMan, font, null);    // default to first available disk
  }

  @Override
  public void activate ()
  {
    tree.setSelectionPath (null);     // turn off any current selection to force an event
    redoHandler.setCurrentData (redoData);
  }

  // connected to RefreshTreeAction
  @Override
  public void refresh ()
  {
    String currentDiskName = ((FileNode) getSelectedObject ()).file.getAbsolutePath ();
    TreeBuilder tb = new TreeBuilder (rootFolder);
    setTree (tb.getTree ());
    if (currentDiskName != null)
      showNode (findNode (currentDiskName));
    setSelectionListener (tree);
  }

  void redoEvent (RedoEvent event)
  {
    DefaultMutableTreeNode node = null;
    if (event.type.equals ("FileNodeEvent"))
    {
      FileNode fn = ((FileNodeSelectedEvent) event.value).getFileNode ();
      node = fn.parentNode;
    }
    else
    {
      FormattedDisk disk = ((DiskSelectedEvent) event.value).getFormattedDisk ();
      node = findNode (disk.getAbsolutePath ());
    }
    if (node == null)
      node = findNode (2);
    if (node != null)
      showNode (node);
    else
      System.out.println ("Disk node not found");
  }

  void selectDisk (String path)
  {
    File file = new File (rootFolder.getAbsolutePath () + path);
    System.out.println (file);
    System.out.println (file.exists ());
    DefaultMutableTreeNode node = findNode (rootFolder.getAbsolutePath () + path);
    if (node != null)
      showNode (node);
    else
    {
      System.out.println ("Path not found: " + rootFolder.getAbsolutePath () + path);
    }
  }

  private DefaultMutableTreeNode findNode (String absolutePath)
  {
    return search (getRootNode (), absolutePath);
  }

  private DefaultMutableTreeNode search (DefaultMutableTreeNode node, String absolutePath)
  {
    FileNode fn = (FileNode) node.getUserObject ();

    int children = node.getChildCount ();
    if (children == 0)
    {
      fn.readFiles ();
      children = node.getChildCount ();
    }

    for (int i = 0; i < children; i++)
    {
      DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt (i);
      FileNode fn2 = (FileNode) childNode.getUserObject ();

      String path = fn2.file.getAbsolutePath ();
      if (absolutePath.equals (path))
        return childNode;

      if (fn2.file.isDirectory () && absolutePath.startsWith (path))
      {
        System.out.println (absolutePath.charAt (path.length ()));
        if (absolutePath.charAt (path.length ()) == File.separatorChar)
        {
          DefaultMutableTreeNode node2 = search (childNode, absolutePath);
          if (node2 != null)
            return node2;
        }
      }
    }

    return null;
  }

  public void replaceDisk (FormattedDisk disk)
  {
    // first check currently selected disk
    FileNode fn = (FileNode) getSelectedObject ();
    if (fn != null && fn.replaceDisk (disk))
      return;

    // find the old disk and replace it
    DefaultMutableTreeNode rootNode = getRootNode ();
    Enumeration<DefaultMutableTreeNode> children = rootNode.breadthFirstEnumeration ();
    while (children.hasMoreElements ())
    {
      DefaultMutableTreeNode node = children.nextElement ();
      fn = (FileNode) node.getUserObject ();
      if (fn.replaceDisk (disk))
        break;
    }
  }

  private void setSelectionListener (JTree tree)
  {
    tree.addTreeSelectionListener (new TreeSelectionListener ()
    {
      @Override
      public void valueChanged (TreeSelectionEvent e)
      {
        FileNode fn = (FileNode) getSelectedObject ();
        if (fn != null)
          eventHandler.fireDiskSelectionEvent (fn);
      }
    });

    tree.addTreeWillExpandListener (new TreeWillExpandListener ()
    {
      @Override
      public void treeWillCollapse (TreeExpansionEvent e) throws ExpandVetoException
      {
      }

      @Override
      public void treeWillExpand (TreeExpansionEvent e) throws ExpandVetoException
      {
        TreePath path = e.getPath ();
        DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) path.getLastPathComponent ();
        FileNode fn = (FileNode) node.getUserObject ();
        if (node.getChildCount () == 0)
          fn.readFiles ();
      }
    });
  }
}