package com.bytezone.diskbrowser.catalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;

class TextDiskCreator extends AbstractDiskCreator
{
  @Override
  public void createDisk ()
  {
    File f = new File ("D:\\DiskDetails.txt");
    FileWriter out = null;

    try
    {
      out = new FileWriter (f);
      printDisk (out);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
    finally
    {
      if (out != null)
        try
        {
          out.close ();
        }
        catch (IOException e)
        {
          e.printStackTrace ();
        }
    }
  }

  private void printDisk (FileWriter out) throws IOException
  {
    Enumeration<DefaultMutableTreeNode> children = getEnumeration ();

    if (children == null)
      return;

    while (children.hasMoreElements ())
    {
      DefaultMutableTreeNode node = children.nextElement ();
      AppleFileSource afs = (AppleFileSource) node.getUserObject ();
      out.write (afs.getDataSource ().getText () + String.format ("%n"));
    }

  }

  @Override
  public String getMenuText ()
  {
    return "create text disk";
  }
}