package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.FormattedDisk;

class AttributeManager extends AbstractFile
{
  List<Statistic> list = new ArrayList<Statistic> ();
  Header header;

  public AttributeManager (String name, byte[] buffer, Header header)
  {
    super (name, buffer);
    this.header = header;

    for (int attrNo = 0; attrNo < 32; attrNo++)
      list.add (new Statistic (attrNo));
  }

  public void addNodes (DefaultMutableTreeNode node, FormattedDisk disk)
  {
    node.setAllowsChildren (true);

    int count = 0;
    for (Statistic stat : list)
    {
      DefaultMutableTreeNode child = new DefaultMutableTreeNode (
          new DefaultAppleFileSource (("Attribute " + count++), stat.getText (), disk));
      node.add (child);
      child.setAllowsChildren (false);
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ("Attribute  Frequency\n");
    text.append ("---------  ---------\n");

    for (Statistic stat : list)
      text.append (String.format ("%s%n", stat));
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  private class Statistic
  {
    int id;
    List<ZObject> list = new ArrayList<ZObject> ();

    public Statistic (int id)
    {
      this.id = id;
      for (ZObject o : header.objectManager)
        if (o.attributes.get (id))
          list.add (o);
    }

    String getText ()
    {
      StringBuilder text =
          new StringBuilder ("Objects with attribute " + id + " set:\n\n");
      for (ZObject o : list)
      {
        text.append (String.format ("%3d  %-28s%n", o.id, o.getName ()));
      }
      if (text.length () > 0)
        text.deleteCharAt (text.length () - 1);
      return text.toString ();
    }

    @Override
    public String toString ()
    {
      return String.format ("   %2d        %3d", id, list.size ());
    }
  }
}