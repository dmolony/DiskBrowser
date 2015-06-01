package com.bytezone.diskbrowser.catalog;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.DiskSelectionListener;

public interface DiskLister extends DiskSelectionListener
{
	public void setDisk (FormattedDisk disk);

	public void createDisk ();

	public Enumeration<DefaultMutableTreeNode> getEnumeration ();

	public String getMenuText ();
}