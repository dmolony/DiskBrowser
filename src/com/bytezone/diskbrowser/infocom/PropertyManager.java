package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.FormattedDisk;

// -----------------------------------------------------------------------------------//
class PropertyManager extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  List<Statistic> list = new ArrayList<> ();
  Header header;

  // ---------------------------------------------------------------------------------//
  PropertyManager (String name, byte[] buffer, Header header)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
    this.header = header;

    for (int propertyNo = 1; propertyNo <= 31; propertyNo++)
    {
      Statistic statistic = new Statistic (propertyNo);
      list.add (statistic);
    }
  }

  // ---------------------------------------------------------------------------------//
  public void addNodes (DefaultMutableTreeNode node, FormattedDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    node.setAllowsChildren (true);

    for (Statistic stat : list)
      if (stat.list.size () > 0)
      {
        String title = "Property " + header.getPropertyName (stat.id).trim ();
        DefaultMutableTreeNode child = new DefaultMutableTreeNode (
            new DefaultAppleFileSource (title, stat.getText (), disk));
        node.add (child);
        child.setAllowsChildren (false);
      }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("Property   Type    Frequency\n");
    text.append ("--------   -----   ---------\n");

    for (Statistic stat : list)
      text.append (String.format ("%s%n", stat));
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private class Statistic
  // ---------------------------------------------------------------------------------//
  {
    int id;
    List<ZObject> list = new ArrayList<> ();

    public Statistic (int id)
    {
      this.id = id;
      for (ZObject o : header.objectManager)
      {
        ZObject.Property p = o.getProperty (id);
        if (p != null)
          list.add (o);
      }
    }

    String getText ()
    {
      StringBuilder text = new StringBuilder (String
          .format ("Objects with property %d %s set:%n%n", id, header.propertyNames[id]));
      for (ZObject o : list)
      {
        ZObject.Property p = o.getProperty (id);
        text.append (String.format ("%02X  %-29s%s%n", o.getId (), o.getName (),
            p.toString ().substring (11)));
      }
      if (text.length () > 0)
        text.deleteCharAt (text.length () - 1);
      return text.toString ();
    }

    @Override
    public String toString ()
    {
      return String.format ("   %2d      %-6s    %3d", id, header.getPropertyName (id),
          list.size ());
    }
  }
}