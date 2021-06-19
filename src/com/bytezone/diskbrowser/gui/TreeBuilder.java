package com.bytezone.diskbrowser.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.bytezone.diskbrowser.disk.DiskFactory;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class TreeBuilder
// -----------------------------------------------------------------------------------//
{
  private static SimpleDateFormat sdf = new SimpleDateFormat ("dd LLL yyyy");

  private final FileComparator fileComparator = new FileComparator ();
  private final JTree tree;

  // ---------------------------------------------------------------------------------//
  TreeBuilder (File rootFolder)
  // ---------------------------------------------------------------------------------//
  {
    assert (rootFolder.exists ());
    assert (rootFolder.isDirectory ());

    FileNode fileNode = new FileNode (rootFolder);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode (fileNode);
    fileNode.setTreeNode (root);

    addFiles (root, rootFolder);
    DefaultTreeModel treeModel = new DefaultTreeModel (root);
    tree = new JTree (treeModel);

    treeModel.setAsksAllowsChildren (true);   // allows empty nodes to appear as folders
    setDiskIcon ("/com/bytezone/diskbrowser/icons/disk.png");
  }

  // ---------------------------------------------------------------------------------//
  JTree getTree ()
  // ---------------------------------------------------------------------------------//
  {
    return tree;
  }

  // ---------------------------------------------------------------------------------//
  private void addFiles (DefaultMutableTreeNode parentNode, File directory)
  // ---------------------------------------------------------------------------------//
  {
    File[] files = directory.listFiles ();
    if (files == null || files.length == 0)
    {
      System.out.println ("Empty folder : " + directory.getAbsolutePath ());
      return;
    }

    Arrays.sort (files, fileComparator);

    for (File file : files)
    {
      if (file.isHidden ())
        continue;
      if (file.isDirectory ())
        parentNode.add (createNode (file, true));
      else if (Utility.validFileType (file.getName ()) && file.length () > 0)
        parentNode.add (createNode (file, false));
    }
  }

  // ---------------------------------------------------------------------------------//
  private DefaultMutableTreeNode createNode (File file, boolean allowsChildren)
  // ---------------------------------------------------------------------------------//
  {
    FileNode fileNode = new FileNode (file);
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (fileNode);
    fileNode.setTreeNode (newNode);
    newNode.setAllowsChildren (allowsChildren);
    return newNode;
  }

  // ---------------------------------------------------------------------------------//
  private void setDiskIcon (String iconName)
  // ---------------------------------------------------------------------------------//
  {
    URL url = this.getClass ().getResource (iconName);
    if (url != null)
    {
      ImageIcon icon = new ImageIcon (url);
      DefaultTreeCellRenderer renderer =
          (DefaultTreeCellRenderer) tree.getCellRenderer ();
      renderer.setLeafIcon (icon);
      tree.setCellRenderer (renderer);
      tree.setRowHeight (18);
    }
    else
      System.out.println ("Failed to set the disk icon : " + iconName);
  }

  // ---------------------------------------------------------------------------------//
  class FileNode implements DataSource
  // ---------------------------------------------------------------------------------//
  {
    private static final int MAX_NAME_LENGTH = 36;
    private static final int SUFFIX_LENGTH = 12;
    private static final int PREFIX_LENGTH = MAX_NAME_LENGTH - SUFFIX_LENGTH - 3;

    DefaultMutableTreeNode parentNode;
    public final File file;
    private FormattedDisk formattedDisk;
    int disks;
    boolean showDisks;

    // -------------------------------------------------------------------------------//
    FileNode (File file)
    // -------------------------------------------------------------------------------//
    {
      this.file = file;
    }

    // -------------------------------------------------------------------------------//
    void setTreeNode (DefaultMutableTreeNode node)
    // -------------------------------------------------------------------------------//
    {
      this.parentNode = node;
    }

    // -------------------------------------------------------------------------------//
    void readFiles ()
    // -------------------------------------------------------------------------------//
    {
      addFiles (parentNode, file);
    }

    // -------------------------------------------------------------------------------//
    FormattedDisk getFormattedDisk ()
    // -------------------------------------------------------------------------------//
    {
      if (formattedDisk == null)
        try
        {
          formattedDisk = DiskFactory.createDisk (file);
        }
        catch (FileFormatException e)
        {
          System.out.println ("Swallowing a FileFormatException in TreeBuilder");
          System.out.println (e.getMessage ());
          return null;
        }
      return formattedDisk;
    }

    // -------------------------------------------------------------------------------//
    boolean replaceDisk (FormattedDisk disk)
    // -------------------------------------------------------------------------------//
    {
      String path = disk.getDisk ().getFile ().getAbsolutePath ();
      if (formattedDisk != null && path.equals (file.getAbsolutePath ()))
      {
        formattedDisk = disk;
        return true;
      }
      return false;
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      String name = file.getName ();
      if (name.length () > MAX_NAME_LENGTH)
        name = name.substring (0, PREFIX_LENGTH) + "..."
            + name.substring (name.length () - SUFFIX_LENGTH);
      if (showDisks && disks > 0)
        return String.format ("%s (%,d)", name, disks);
      return name;
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String getText ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      String home = System.getProperty ("user.home");
      String path = file.getAbsolutePath ();
      if (path.startsWith (home))
        path = "~" + path.substring (home.length ());
      text.append ("Directory : " + path + "\n\n");
      text.append ("D         File names                       "
          + "      Date               Size  Type\n");
      text.append ("-  ----------------------------------------"
          + "  -----------  --------------  ---------\n");

      File[] files = file.listFiles ();
      if (files != null)
      {
        Arrays.sort (files, fileComparator);
        for (File f : files)
        {
          if (f.isHidden ())
            continue;
          String name = f.getName ();

          Date d = new Date (f.lastModified ());
          int pos = name.lastIndexOf ('.');
          String type = pos > 0 && !f.isDirectory () ? name.substring (pos) : "";
          String size = f.isDirectory () ? "" : String.format ("%,14d", f.length ());
          text.append (String.format ("%s  %-40.40s  %s  %-14s  %s%n",
              f.isDirectory () ? "D" : " ", name, sdf.format (d), size, type));
        }
      }

      if (text.length () > 0)
        text.deleteCharAt (text.length () - 1);
      return text.toString ();
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String getAssembler ()
    // -------------------------------------------------------------------------------//
    {
      return null;
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String getHexDump ()
    // -------------------------------------------------------------------------------//
    {
      return null;
    }

    // -------------------------------------------------------------------------------//
    @Override
    public BufferedImage getImage ()
    // -------------------------------------------------------------------------------//
    {
      return null;
    }

    // ---------------------------------------------------------------------------------//
    @Override
    public byte[] getBuffer ()
    // ---------------------------------------------------------------------------------//
    {
      return null;
    }

    // -------------------------------------------------------------------------------//
    @Override
    public JComponent getComponent ()
    // -------------------------------------------------------------------------------//
    {
      return null;
    }
  }
}