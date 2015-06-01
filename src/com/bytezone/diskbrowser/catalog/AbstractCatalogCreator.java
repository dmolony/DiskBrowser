package com.bytezone.diskbrowser.catalog;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractCatalogCreator implements CatalogLister
{
	DefaultMutableTreeNode node;

	public void setNode (DefaultMutableTreeNode node)
	{
		this.node = node;
	}
}