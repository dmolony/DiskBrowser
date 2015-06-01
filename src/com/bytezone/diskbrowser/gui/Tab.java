package com.bytezone.diskbrowser.gui;

/***********************************************************************************************
 * Interface implemented by AbstractTab, and in turn FileSystemTab and AppleDiskTab.
 * 
 * 
 ***********************************************************************************************/

import javax.swing.tree.DefaultMutableTreeNode;

interface Tab
{
  public void refresh ();

  public void activate ();

  public DefaultMutableTreeNode getRootNode ();
}

// public void addMouseListener (MouseAdapter ma)