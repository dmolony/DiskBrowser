package com.bytezone.diskbrowser.catalog;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.disk.DiskFactory;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.TreeBuilder.FileNode;

public class TextCatalogCreator extends AbstractCatalogCreator
{
  @Override
  public void createCatalog ()
  {
    Object o = node.getUserObject ();
    if (!(o instanceof FileNode))
    {
      JOptionPane.showMessageDialog (null, "Please select a folder from the Disk Tree",
                                     "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    File f = ((FileNode) o).file;
    final File f2 = new File (f.getAbsolutePath () + "/Catalog.txt");
    JOptionPane.showMessageDialog (null, "About to create file : " + f2.getAbsolutePath (),
                                   "Info", JOptionPane.INFORMATION_MESSAGE);

    EventQueue.invokeLater (new Runnable ()
    {
      @Override
      public void run ()
      {
        FileWriter out = null;
        try
        {
          out = new FileWriter (f2);
          printDescendants (node, out);
        }
        catch (IOException e)
        {
          JOptionPane.showMessageDialog (null, "Error creating catalog : " + e.getMessage (),
                                         "Bugger", JOptionPane.INFORMATION_MESSAGE);
        }
        finally
        {
          try
          {
            if (out != null)
              out.close ();
          }
          catch (IOException e)
          {
            e.printStackTrace ();
          }
        }
      }

      private void printDescendants (DefaultMutableTreeNode root, FileWriter out)
            throws IOException
      {
        Object o = root.getUserObject ();
        if (o instanceof FileNode)
        {
          File f = ((FileNode) root.getUserObject ()).file;
          if (!f.isDirectory ())
          {
            FormattedDisk fd = DiskFactory.createDisk (f.getAbsolutePath ());
            out.write (fd.getCatalog ().getDataSource ().getText () + String.format ("%n"));
          }
        }

        Enumeration<DefaultMutableTreeNode> children = root.children ();
        if (children != null)
          while (children.hasMoreElements ())
            printDescendants (children.nextElement (), out);
      }
    });
  }

  @Override
  public String getMenuText ()
  {
    return "Create catalog text";
  }
}