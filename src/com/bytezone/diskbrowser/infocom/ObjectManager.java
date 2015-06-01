package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.FormattedDisk;

class ObjectManager extends AbstractFile implements Iterable<ZObject>
{
	Header header;
	List<ZObject> list;
	int defaultsPtr, defaultsSize;
	int tablePtr, tableSize;
	int propertyPtr, propertySize;
	ObjectAnalyser analyser;

	public ObjectManager (Header header)
	{
		super ("Objects", header.buffer);
		this.header = header;

		defaultsPtr = header.objectTable;
		defaultsSize = 62;
		tablePtr = header.objectTable + 62;
		propertyPtr = header.getWord (tablePtr + 7);
		propertySize = header.globalsOffset - propertyPtr;
		tableSize = (propertyPtr - tablePtr);
		int totalObjects = tableSize / ZObject.HEADER_SIZE;
		list = new ArrayList<ZObject> (tableSize);

		for (int objectNo = 0; objectNo < totalObjects; objectNo++)
			list.add (new ZObject (null, buffer, tablePtr + objectNo * ZObject.HEADER_SIZE, objectNo + 1,
						header));

		// analyse objects - set stringPtr etc.
		analyser = new ObjectAnalyser (header, this);

		// add entries for AbstractFile.getHexDump ()
		hexBlocks.add (new HexBlock (defaultsPtr, defaultsSize, "Property defaults:"));
		hexBlocks.add (new HexBlock (tablePtr, tableSize, "Objects table:"));
		hexBlocks.add (new HexBlock (propertyPtr, propertySize, "Properties:"));
	}

	public void addNodes (DefaultMutableTreeNode root, FormattedDisk disk)
	{
		root.setAllowsChildren (true);

		for (ZObject zo : list)
			if (zo.parent == 0)
				buildObjectTree (zo, root, disk);
	}

	private void buildObjectTree (ZObject object, DefaultMutableTreeNode parentNode,
				FormattedDisk disk)
	{
		DefaultMutableTreeNode child =
					new DefaultMutableTreeNode (new DefaultAppleFileSource (object.name, object, disk));
		parentNode.add (child);
		if (object.sibling > 0)
			buildObjectTree (header.objectManager.list.get (object.sibling - 1), parentNode, disk);
		if (object.child > 0)
			buildObjectTree (header.objectManager.list.get (object.child - 1), child, disk);
		else
			child.setAllowsChildren (false);
	}

	public List<Integer> getCodeRoutines ()
	{
		return analyser.routines;
	}

	@Override
	public String getText ()
	{
		StringBuilder text =
					new StringBuilder ("  #   Attributes  Pr Sb Ch  Prop   Title\n---   -----------"
								+ " -- -- -- -----   -----------------------------\n");

		int objectNumber = 0;
		for (ZObject zo : list)
			text.append (String.format ("%3d   %s%n", ++objectNumber, zo));
		text.deleteCharAt (text.length () - 1);
		return text.toString ();
	}

	@Override
	public Iterator<ZObject> iterator ()
	{
		return list.iterator ();
	}
}