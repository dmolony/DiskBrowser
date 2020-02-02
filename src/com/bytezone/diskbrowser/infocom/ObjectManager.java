package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.FormattedDisk;

class ObjectManager extends InfocomAbstractFile implements Iterable<ZObject>
{
  //  private final Header header;
  private final List<ZObject> list;
  private List<ZObject> sortedList;
  private final int defaultsPtr, defaultsSize;
  private final int tablePtr, tableSize;
  private final int propertyPtr, propertySize;
  private final ObjectAnalyser analyser;

  public ObjectManager (Header header)
  {
    super ("Objects", header.buffer);
    //    this.header = header;

    defaultsPtr = header.objectTableOffset;
    defaultsSize = 62;                                // 31 words
    tablePtr = header.objectTableOffset + 62;
    propertyPtr = header.getWord (tablePtr + 7);
    propertySize = header.globalsOffset - propertyPtr;
    tableSize = (propertyPtr - tablePtr);
    int totalObjects = tableSize / ZObject.HEADER_SIZE;
    list = new ArrayList<> (tableSize);

    for (int objectNo = 0; objectNo < totalObjects; objectNo++)
      list.add (new ZObject (null, buffer, tablePtr + objectNo * ZObject.HEADER_SIZE,
          objectNo + 1, header));

    // analyse objects - set stringPtr etc.
    analyser = new ObjectAnalyser (header, this);

    // add entries for AbstractFile.getHexDump ()
    hexBlocks.add (new HexBlock (defaultsPtr, defaultsSize, "Property defaults:"));
    hexBlocks.add (new HexBlock (tablePtr, tableSize, "Objects table:"));
    hexBlocks.add (new HexBlock (propertyPtr, propertySize, "Properties:"));
  }

  List<ZObject> getObjects ()
  {
    return list;
  }

  ZObject getObject (int index)
  {
    if (index < 0 || index >= list.size ())
    {
      System.out.printf ("Invalid index: %d / %d%n", index, list.size ());
      return null;
    }
    return list.get (index);
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
    DefaultMutableTreeNode child = new DefaultMutableTreeNode (
        new DefaultAppleFileSource (object.getName (), object, disk));
    parentNode.add (child);
    if (object.sibling > 0)
      buildObjectTree (list.get (object.sibling - 1), parentNode, disk);
    if (object.child > 0)
      buildObjectTree (list.get (object.child - 1), child, disk);
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
    //    String header1 = "ID   Attributes  Pr Sb Ch  Prop   Title\n--   -----------"
    //        + " -- -- -- -----   -----------------------------\n";
    String underline = " ----------------------------------------";
    String titles[] =
        { "ID  ", "Title                                    ",
          "Parent                                   ",
          "Sibling                                  ",
          "Child                                    ", "Attributes   Prop\n" };
    String header2 = titles[0] + titles[1] + titles[2] + titles[3] + titles[4] + titles[5]
        + "-- " + underline + underline + underline + underline + " -----------  -----\n";
    StringBuilder text = new StringBuilder (header2);

    if (sortedList == null)
      sortedList = new ArrayList<> (list);
    Collections.sort (sortedList);

    //    int objectNumber = 0;
    for (ZObject zo : list)
      //      if (false)
      //        text.append (String.format ("%02X   %s%n", ++objectNumber, zo));
      //      else
      text.append (String.format ("%02X %s%n", zo.getId (), zo.getDescription (list)));

    text.append ("\n\n");
    text.append (header2);
    for (ZObject zo : sortedList)
      text.append (String.format ("%02X %s%n", zo.getId (), zo.getDescription (list)));

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  @Override
  public Iterator<ZObject> iterator ()
  {
    return list.iterator ();
  }
}