package com.bytezone.diskbrowser.catalog;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.DiskSelectedEvent;

public abstract class AbstractDiskCreator implements DiskLister
{
  FormattedDisk disk;

  public void setDisk (FormattedDisk disk)
  {
    this.disk = disk;
  }

  // should return List<AppleFileSource>
  @SuppressWarnings("unchecked")
  public Enumeration<DefaultMutableTreeNode> getEnumeration ()
  {
    JTree tree = disk.getCatalogTree ();
    DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel ();
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel.getRoot ();
    return node.breadthFirstEnumeration ();
  }

  public void diskSelected (DiskSelectedEvent e)
  {
    setDisk (e.getFormattedDisk ());
  }
}