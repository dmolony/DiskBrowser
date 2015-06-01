package com.bytezone.diskbrowser.catalog;

import javax.swing.tree.DefaultMutableTreeNode;

public interface CatalogLister
{
	public void setNode (DefaultMutableTreeNode node);

	public void createCatalog ();

	public String getMenuText ();
}