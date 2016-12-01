package com.bytezone.diskbrowser.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskFactory;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.utilities.FileFormatException;

public class TreeBuilder
{
  private static SimpleDateFormat sdf = new SimpleDateFormat ("dd MMM yyyy");
  private static final boolean FULL_TREE = false;
  private static final List<String> suffixes =
      Arrays.asList ("po", "dsk", "do", "hdv", "2mg", "v2d", "nib", "d13", "sdk", "gz");

  FileComparator fc = new FileComparator ();
  JTree tree;
  int totalDisks;
  int totalFolders;

  Map<String, Integer> totalFiles = new TreeMap<String, Integer> ();

  Map<String, List<DiskDetails>> duplicateDisks =
      new TreeMap<String, List<DiskDetails>> ();
  Map<String, File> diskNames = new HashMap<String, File> ();
  Map<Long, List<File>> dosMap = new TreeMap<Long, List<File>> ();

  public TreeBuilder (File folder)
  {
    assert (folder.exists ());
    assert (folder.isDirectory ());

    FileNode fn = new FileNode (folder);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode (fn);
    fn.setTreeNode (root);
    addFiles (root, folder);
    DefaultTreeModel treeModel = new DefaultTreeModel (root);
    tree = new JTree (treeModel);

    treeModel.setAsksAllowsChildren (true);   // allows empty nodes to appear as folders
    setDiskIcon ("/com/bytezone/diskbrowser/icons/disk.png");
    ((FileNode) root.getUserObject ()).disks = totalDisks;

    if (FULL_TREE)
    {
      System.out.printf ("%nFolders ..... %,5d%n", totalFolders);
      System.out.printf ("Disks ....... %,5d%n%n", totalDisks);

      int tf = 0;
      for (String key : totalFiles.keySet ())
      {
        int t = totalFiles.get (key);
        tf += t;
        System.out.printf ("%13.13s %,5d%n", key + " ...........", t);
      }
      System.out.printf ("%nTotal ...... %,6d%n%n", tf);
    }
  }

  private void addFiles (DefaultMutableTreeNode node, File directory)
  {
    File[] files = directory.listFiles ();
    if (files == null || files.length == 0)
    {
      System.out.println ("Empty folder : " + directory.getAbsolutePath ());
      return;
    }

    FileNode parentNode = (FileNode) node.getUserObject ();
    Arrays.sort (files, fc);
    for (File file : files)
    {
      if (file.isDirectory ())
      {
        FileNode fn = new FileNode (file);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (fn);
        fn.setTreeNode (newNode);
        newNode.setAllowsChildren (true);
        node.add (newNode);
        totalFolders++;

        if (FULL_TREE)
          addFiles (newNode, file);             // recursion!
        continue;
      }

      if (FULL_TREE)
      {
        int pos = file.getName ().lastIndexOf ('.');
        if (pos > 0)
        {
          String type = file.getName ().substring (pos + 1).toLowerCase ();
          if (totalFiles.containsKey (type))
          {
            int t = totalFiles.get (type);
            totalFiles.put (type, ++t);
          }
          else
            totalFiles.put (type, 1);
        }
      }

      if (file.length () != 143360 && file.length () != 116480 && file.length () != 819264
          && file.length () < 200000)
      {
        String name = file.getName ().toLowerCase ();
        if (!name.endsWith (".sdk") && !name.endsWith (".dsk.gz"))
          continue;
      }

      parentNode.disks++;
      String filename = file.getAbsolutePath ();
      if (validFileType (filename))
      {
        FileNode fn = new FileNode (file);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (fn);
        fn.setTreeNode (newNode);
        newNode.setAllowsChildren (false);
        node.add (newNode);

        if (false)
          checkDuplicates (file);

        totalDisks++;

        if (false)
          checksumDos (file);
      }
    }
  }

  private void checksumDos (File file)
  {
    if (file.length () != 143360 || file.getAbsolutePath ().contains ("/ZDisks/"))
      return;

    Disk disk = new AppleDisk (file, 35, 16);
    byte[] buffer = disk.readSector (0, 0);

    Checksum checksum = new CRC32 ();
    checksum.update (buffer, 0, buffer.length);
    long cs = checksum.getValue ();
    List<File> files = dosMap.get (cs);
    if (files == null)
    {
      files = new ArrayList<File> ();
      dosMap.put (cs, files);
    }
    files.add (file);
  }

  private void checkDuplicates (File file)
  {
    if (diskNames.containsKey (file.getName ()))
    {
      List<DiskDetails> diskList = duplicateDisks.get (file.getName ());
      if (diskList == null)
      {
        diskList = new ArrayList<DiskDetails> ();
        duplicateDisks.put (file.getName (), diskList);
        diskList.add (new DiskDetails (diskNames.get (file.getName ())));// add the original
      }
      diskList.add (new DiskDetails (file));// add the duplicate
    }
    else
      diskNames.put (file.getName (), file);
  }

  private boolean validFileType (String filename)
  {
    int dotPos = filename.lastIndexOf ('.');
    if (dotPos < 0)
      return false;

    String suffix = filename.substring (dotPos + 1).toLowerCase ();

    int dotPos2 = filename.lastIndexOf ('.', dotPos - 1);
    if (dotPos2 > 0)
    {
      String suffix2 = filename.substring (dotPos2 + 1, dotPos).toLowerCase ();
      if (suffix.equals ("gz") && (suffix2.equals ("bxy") || suffix2.equals ("bny")))
        return false;
    }

    return suffixes.contains (suffix);
  }

  private void setDiskIcon (String iconName)
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

  /*
   * Class used to control the text displayed by the JTree.
   */
  public class FileNode implements DataSource
  {
    DefaultMutableTreeNode parentNode;
    public final File file;
    private static final int MAX_NAME_LENGTH = 36;
    private static final int SUFFIX_LENGTH = 12;
    private static final int PREFIX_LENGTH = MAX_NAME_LENGTH - SUFFIX_LENGTH - 3;
    private FormattedDisk formattedDisk;
    int disks;
    boolean showDisks;

    public FileNode (File file)
    {
      this.file = file;
    }

    public void setTreeNode (DefaultMutableTreeNode node)
    {
      this.parentNode = node;
    }

    public void readFiles ()
    {
      addFiles (parentNode, file);
    }

    public FormattedDisk getFormattedDisk ()
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

    public boolean replaceDisk (FormattedDisk disk)
    {
      String path = disk.getDisk ().getFile ().getAbsolutePath ();
      if (formattedDisk != null && path.equals (file.getAbsolutePath ()))
      {
        formattedDisk = disk;
        return true;
      }
      return false;
    }

    @Override
    public String toString ()
    {
      String name = file.getName ();
      if (name.length () > MAX_NAME_LENGTH)
        name = name.substring (0, PREFIX_LENGTH) + "..."
            + name.substring (name.length () - SUFFIX_LENGTH);
      if (showDisks && disks > 0)
        return String.format ("%s (%,d)", name, disks);
      return name;
    }

    @Override
    public String getText ()
    {
      StringBuilder text = new StringBuilder ();

      text.append ("Directory : " + file.getAbsolutePath () + "\n\n");
      text.append ("D         File names                       "
          + "      Date               Size  Type\n");
      text.append ("-  ----------------------------------------"
          + "  -----------  --------------  ---------\n");

      File[] files = file.listFiles ();
      if (files != null)
        for (File f : files)
        {
          String name = f.getName ();
          if (name.startsWith ("."))
            continue;

          Date d = new Date (f.lastModified ());
          int pos = name.lastIndexOf ('.');
          String type = pos > 0 && !f.isDirectory () ? name.substring (pos) : "";
          String size = f.isDirectory () ? "" : String.format ("%,14d", f.length ());
          text.append (String.format ("%s  %-40.40s  %s  %-14s  %s%n",
              f.isDirectory () ? "D" : " ", name, sdf.format (d), size, type));
        }

      if (text.length () > 0)
        text.deleteCharAt (text.length () - 1);
      return text.toString ();
    }

    @Override
    public String getAssembler ()
    {
      return null;
    }

    @Override
    public String getHexDump ()
    {
      return null;
    }

    @Override
    public BufferedImage getImage ()
    {
      return null;
    }

    @Override
    public JComponent getComponent ()
    {
      return null;
    }
  }

  private class FileComparator implements Comparator<File>
  {
    @Override
    public int compare (File filea, File fileb)
    {
      boolean fileaIsDirectory = filea.isDirectory ();
      boolean filebIsDirectory = fileb.isDirectory ();

      if (fileaIsDirectory && !filebIsDirectory)
        return -1;
      if (!fileaIsDirectory && filebIsDirectory)
        return 1;
      return filea.getName ().compareToIgnoreCase (fileb.getName ());
    }
  }
}