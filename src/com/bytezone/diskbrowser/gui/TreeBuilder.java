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

public class TreeBuilder
{
  private static SimpleDateFormat sdf = new SimpleDateFormat ("dd MMM yyyy");
  private static final int DISK_13_SIZE = 116480;
  private static final int DISK_16_SIZE = 143360;
  private static final int DISK_800K_SIZE = 819264;

  //  private static final boolean FULL_TREE_TRAVERSAL = false;

  private final FileComparator fileComparator = new FileComparator ();
  private final JTree tree;

  //  private int totalDisks;
  //  private int totalFolders;

  //  // total files for each suffix
  //  private final Map<String, Integer> typeList = new TreeMap<String, Integer> ();
  //
  //  // list of unique disk names -> List of File duplicates
  //  final Map<String, List<DiskDetails>> duplicateDisks =
  //      new TreeMap<String, List<DiskDetails>> ();
  //
  //  // list of unique disk names -> File
  //  private final Map<String, File> diskNames = new HashMap<String, File> ();
  //
  //  // list of checksum -> File
  //  final Map<Long, List<File>> dosMap = new TreeMap<Long, List<File>> ();

  public TreeBuilder (File folder)
  {
    assert (folder.exists ());
    assert (folder.isDirectory ());

    FileNode fileNode = new FileNode (folder);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode (fileNode);
    fileNode.setTreeNode (root);

    addFiles (root, folder);
    DefaultTreeModel treeModel = new DefaultTreeModel (root);
    tree = new JTree (treeModel);

    treeModel.setAsksAllowsChildren (true);   // allows empty nodes to appear as folders
    setDiskIcon ("/com/bytezone/diskbrowser/icons/disk.png");
    //    ((FileNode) root.getUserObject ()).disks = totalDisks;

    //    if (FULL_TREE_TRAVERSAL)
    //    {
    //      System.out.printf ("%nFolders ..... %,5d%n", totalFolders);
    //      System.out.printf ("Disks ....... %,5d%n%n", totalDisks);
    //
    //      int grandTotal = 0;
    //      for (String key : typeList.keySet ())
    //      {
    //        int typeTotal = typeList.get (key);
    //        grandTotal += typeTotal;
    //        System.out.printf ("%13.13s %,6d%n", key + " ...........", typeTotal);
    //      }
    //      System.out.printf ("%nTotal ....... %,6d%n%n", grandTotal);
    //    }
  }

  public JTree getTree ()
  {
    return tree;
  }

  private void addFiles (DefaultMutableTreeNode node, File directory)
  {
    File[] files = directory.listFiles ();
    if (files == null || files.length == 0)
    {
      System.out.println ("Empty folder : " + directory.getAbsolutePath ());
      return;
    }

    //    FileNode parentNode = (FileNode) node.getUserObject ();
    Arrays.sort (files, fileComparator);

    for (File file : files)
    {
      if (file.isDirectory ())
      {
        FileNode fileNode = new FileNode (file);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (fileNode);
        fileNode.setTreeNode (newNode);
        newNode.setAllowsChildren (true);
        node.add (newNode);

        //        totalFolders++;

        //        if (FULL_TREE_TRAVERSAL)
        //          addFiles (newNode, file);             // recursion!

        continue;
      }

      //      if (FULL_TREE_TRAVERSAL)
      //      {
      //        int pos = file.getName ().lastIndexOf ('.');
      //        if (pos > 0)
      //        {
      //          String type = file.getName ().substring (pos + 1).toLowerCase ();
      //          if (typeList.containsKey (type))
      //          {
      //            int t = typeList.get (type);
      //            typeList.put (type, ++t);
      //          }
      //          else
      //            typeList.put (type, 1);
      //        }
      //      }

      if (file.length () != DISK_16_SIZE && file.length () != DISK_13_SIZE
          && file.length () != DISK_800K_SIZE && file.length () < 200000)
      {
        String name = file.getName ().toLowerCase ();
        if (!name.endsWith (".sdk") && !name.endsWith (".dsk.gz"))
          continue;
      }

      //      parentNode.disks++;

      if (Utility.validFileType (file.getAbsolutePath ()))
      {
        FileNode fileNode = new FileNode (file);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (fileNode);
        fileNode.setTreeNode (newNode);
        newNode.setAllowsChildren (false);
        node.add (newNode);

        //        if (false)
        //          checkDuplicates (file);

        //        totalDisks++;

        //        if (false)
        //          checksumDos (file);
      }
    }
  }

  //  private void checksumDos (File file)
  //  {
  //    if (file.length () != 143360 || file.getAbsolutePath ().contains ("/ZDisks/"))
  //      return;
  //
  //    Disk disk = new AppleDisk (file, 35, 16);
  //    byte[] buffer = disk.readSector (0, 0);
  //
  //    Checksum checksum = new CRC32 ();
  //    checksum.update (buffer, 0, buffer.length);
  //    long cs = checksum.getValue ();
  //    List<File> files = dosMap.get (cs);
  //    if (files == null)
  //    {
  //      files = new ArrayList<File> ();
  //      dosMap.put (cs, files);
  //    }
  //    files.add (file);
  //  }

  //  private void checkDuplicates (File file)
  //  {
  //    if (diskNames.containsKey (file.getName ()))
  //    {
  //      List<DiskDetails> diskList = duplicateDisks.get (file.getName ());
  //      if (diskList == null)
  //      {
  //        diskList = new ArrayList<DiskDetails> ();
  //        duplicateDisks.put (file.getName (), diskList);
  //        diskList.add (new DiskDetails (diskNames.get (file.getName ())));// add original
  //      }
  //      diskList.add (new DiskDetails (file));                        // add the duplicate
  //    }
  //    else
  //      diskNames.put (file.getName (), file);
  //  }

  //  private boolean validFileType (String filename)
  //  {
  //    int dotPos = filename.lastIndexOf ('.');
  //    if (dotPos < 0)
  //      return false;
  //
  //    String suffix = filename.substring (dotPos + 1).toLowerCase ();
  //
  //    int dotPos2 = filename.lastIndexOf ('.', dotPos - 1);
  //    if (dotPos2 > 0)
  //    {
  //      String suffix2 = filename.substring (dotPos2 + 1, dotPos).toLowerCase ();
  //      if (suffix.equals ("gz") && (suffix2.equals ("bxy") || suffix2.equals ("bny")))
  //        return false;
  //    }
  //
  //    return suffixes.contains (suffix);
  //  }

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

  //  private class FileComparator implements Comparator<File>
  //  {
  //    @Override
  //    public int compare (File thisFile, File thatFile)
  //    {
  //      boolean thisFileIsDirectory = thisFile.isDirectory ();
  //      boolean thatFileIsDirectory = thatFile.isDirectory ();
  //
  //      if (thisFileIsDirectory && !thatFileIsDirectory)
  //        return -1;
  //      if (!thisFileIsDirectory && thatFileIsDirectory)
  //        return 1;
  //
  //      return thisFile.getName ().compareToIgnoreCase (thatFile.getName ());
  //    }
  //  }
}