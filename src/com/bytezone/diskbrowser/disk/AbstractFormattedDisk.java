package com.bytezone.diskbrowser.disk;

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.BootSector;
import com.bytezone.diskbrowser.gui.DataSource;

// -----------------------------------------------------------------------------------//
public abstract class AbstractFormattedDisk implements FormattedDisk
// -----------------------------------------------------------------------------------//
{
  protected Disk disk;
  protected FormattedDisk parent;                 // used by Dual-dos disks

  protected ActionListener actionListenerList;
  protected JTree catalogTree;
  protected Path originalPath;

  protected List<SectorType> sectorTypesList = new ArrayList<> ();
  protected List<AppleFileSource> fileEntries = new ArrayList<> ();

  public SectorType[] sectorTypes;

  protected BootSector bootSector;

  public final SectorType emptySector = new SectorType ("Unused (empty)", Color.white);
  public final SectorType usedSector = new SectorType ("Unused (data)", Color.yellow);

  protected int falsePositives;
  protected int falseNegatives;

  protected Dimension gridLayout;

  protected BitSet freeBlocks;
  protected BitSet usedBlocks; // still to be populated - currently using stillAvailable ()

  // ---------------------------------------------------------------------------------//
  public AbstractFormattedDisk (Disk disk)
  // ---------------------------------------------------------------------------------//
  {
    this.disk = disk;
    freeBlocks = new BitSet (disk.getTotalBlocks ());
    usedBlocks = new BitSet (disk.getTotalBlocks ());
    /*
     * All formatted disks will have empty and/or used sectors, so set them
     * here, and let the actual subclass add its sector types later. This list
     * is used to hold one of each sector type so that the DiskLayoutPanel can
     * draw its grid and key correctly. Every additional type that the instance
     * creates should be added here too.
     */
    sectorTypesList.add (emptySector);
    sectorTypesList.add (usedSector);

    /*
     * Hopefully every used sector will be changed by the subclass to something
     * sensible, but deleted files will always leave the sector as used/unknown
     * as it contains data.
     */
    setSectorTypes ();
    setGridLayout ();

    /*
     * Create the disk name as the root for the catalog tree. Subclasses will
     * have to append their catalog entries to this node.
     */
    String name = getName ();
    if (name.endsWith (".tmp"))
      name = "tmp.dsk";

    DefaultAppleFileSource afs =
        new DefaultAppleFileSource (name, disk.toString (), this);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode (afs);
    DefaultTreeModel treeModel = new DefaultTreeModel (root);
    catalogTree = new JTree (treeModel);
    treeModel.setAsksAllowsChildren (true); // allows empty nodes to appear as folders

    /*
     * Add an ActionListener to the disk in case the interleave or blocksize
     * changes
     */
    disk.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setSectorTypes ();
      }
    });
  }

  // ---------------------------------------------------------------------------------//
  protected void setEmptyByte (byte value)
  // ---------------------------------------------------------------------------------//
  {
    getDisk ().setEmptyByte (value);
    setSectorTypes ();
  }

  // ---------------------------------------------------------------------------------//
  private void setSectorTypes ()
  // ---------------------------------------------------------------------------------//
  {
    sectorTypes = new SectorType[disk.getTotalBlocks ()];

    for (DiskAddress da : disk)
      sectorTypes[da.getBlockNo ()] = disk.isBlockEmpty (da) ? emptySector : usedSector;

    setGridLayout ();
  }

  // ---------------------------------------------------------------------------------//
  private void setGridLayout ()
  // ---------------------------------------------------------------------------------//
  {
    int totalBlocks = disk.getTotalBlocks ();

    Dimension newGridLayout = switch (totalBlocks)
    {
      case 280 -> new Dimension (8, 35);
      case 455 -> new Dimension (13, 35);
      case 560 -> new Dimension (16, 35);
      case 704 -> new Dimension (16, 44);
      case 768 -> new Dimension (16, 48);
      case 800 -> new Dimension (8, 100);
      case 1600 ->
      {
        if (disk.getBlocksPerTrack () == 32)
          yield new Dimension (32, 50);
        else
          yield new Dimension (16, 100);
      }
      case 2048 -> new Dimension (8, 256);
      case 3200 -> new Dimension (16, 200);
      default ->
      {
        int[] sizes = { 32, 20, 16, 8 };
        for (int size : sizes)
          if ((totalBlocks % size) == 0)
            yield new Dimension (size, totalBlocks / size);
        yield null;
      }
    };

    if (newGridLayout == null)
      System.out.println ("Unusable total blocks : " + totalBlocks);
    else
      gridLayout = newGridLayout;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Dimension getGridLayout ()
  // ---------------------------------------------------------------------------------//
  {
    return gridLayout;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Disk getDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return disk;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public FormattedDisk getParent ()
  // ---------------------------------------------------------------------------------//
  {
    return parent;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setParent (FormattedDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    parent = disk;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setOriginalPath (Path path)
  // ---------------------------------------------------------------------------------//
  {
    this.originalPath = path;

    DefaultMutableTreeNode root = getCatalogTreeRoot ();
    if (root.getUserObject () == null)
      root.setUserObject (
          new DefaultAppleFileSource (getName (), disk.toString (), this));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Path getOriginalPath ()
  // ---------------------------------------------------------------------------------//
  {
    return originalPath;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getAbsolutePath ()
  // ---------------------------------------------------------------------------------//
  {
    if (originalPath != null)
      return originalPath.toString ();

    return disk.getFile ().getAbsolutePath ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getDisplayPath ()
  // ---------------------------------------------------------------------------------//
  {
    //    if (originalPath != null)
    //      return originalPath.toString ();

    String home = System.getProperty ("user.home");

    String path = originalPath != null ? originalPath.toString ()
        : disk.getFile ().getAbsolutePath ();
    if (path.startsWith (home))
      return "~" + path.substring (home.length ());

    return disk.getFile ().getAbsolutePath ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isTempDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return originalPath != null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    if (originalPath != null)
    {
      Path path = originalPath.getFileName ();
      if (path != null)
        return path.toString ();
    }
    return disk.getFile ().getName ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void writeFile (AbstractFile file)
  // ---------------------------------------------------------------------------------//
  {
    System.out.println ("not implemented yet");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<AppleFileSource> getCatalogList ()
  // ---------------------------------------------------------------------------------//
  {
    return fileEntries;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AppleFileSource getFile (String uniqueName)
  // ---------------------------------------------------------------------------------//
  {
    for (AppleFileSource afs : fileEntries)
      if (afs.getUniqueName ().equals (uniqueName))
        return afs;
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public JTree getCatalogTree ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogTree;
  }

  // ---------------------------------------------------------------------------------//
  public DefaultMutableTreeNode getCatalogTreeRoot ()
  // ---------------------------------------------------------------------------------//
  {
    return (DefaultMutableTreeNode) catalogTree.getModel ().getRoot ();
  }

  // ---------------------------------------------------------------------------------//
  public void makeNodeVisible (DefaultMutableTreeNode node)
  // ---------------------------------------------------------------------------------//
  {
    catalogTree.makeVisible (
        new TreePath (((DefaultTreeModel) catalogTree.getModel ()).getPathToRoot (node)));
  }

  // ---------------------------------------------------------------------------------//
  protected DefaultMutableTreeNode findNode (DefaultMutableTreeNode node, String name)
  // ---------------------------------------------------------------------------------//
  {
    Enumeration<TreeNode> children = node.breadthFirstEnumeration ();
    if (children != null)
      while (children.hasMoreElements ())
      {
        DefaultMutableTreeNode childNode =
            (DefaultMutableTreeNode) children.nextElement ();
        if (childNode.getUserObject ().toString ().indexOf (name) > 0)
          return childNode;
      }

    return null;
  }

  /*
   * These routines just hand back the information that was created above, and
   * added to by the subclass.
   */
  // ---------------------------------------------------------------------------------//
  @Override
  public SectorType getSectorType (int block)
  // ---------------------------------------------------------------------------------//
  {
    return getSectorType (disk.getDiskAddress (block));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public SectorType getSectorType (int track, int sector)
  // ---------------------------------------------------------------------------------//
  {
    return getSectorType (disk.getDiskAddress (track, sector));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public SectorType getSectorType (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return sectorTypes[da.getBlockNo ()];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<SectorType> getSectorTypeList ()
  // ---------------------------------------------------------------------------------//
  {
    return sectorTypesList;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setSectorType (int block, SectorType type)
  // ---------------------------------------------------------------------------------//
  {
    if (block < sectorTypes.length)
      sectorTypes[block] = type;
    else
      System.out.println ("setSectorType: Invalid block number: " + block);
  }

  // Override this so that the correct sector type can be displayed
  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getFormattedSector (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    if (da.isZero () && bootSector != null)
      return bootSector;

    SectorType sectorType = sectorTypes[da.getBlockNo ()];
    byte[] buffer = disk.readBlock (da);

    //    String address = String.format ("%02X %02X", da.getTrackNo (), da.getSectorNo ());
    String address = String.format ("%02X", da.getBlockNo ());

    if (sectorType == emptySector)
      return new DefaultSector ("Empty sector at " + address, disk, buffer, da);
    if (sectorType == usedSector)
      return new DefaultSector ("Orphan sector at " + address, disk, buffer, da);

    String name = getSectorFilename (da);
    if (!name.isEmpty ())
      name = " : " + name;

    return new DefaultSector ("Data sector at " + address + name, disk, buffer, da);
  }

  /*
   * Override this with something useful
   */
  // ---------------------------------------------------------------------------------//
  @Override
  public AppleFileSource getCatalog ()
  // ---------------------------------------------------------------------------------//
  {
    return new DefaultAppleFileSource (disk.toString (), this);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getSectorFilename (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    for (AppleFileSource entry : fileEntries)
      if (entry.contains (da))
        return (entry).getUniqueName ();

    return "";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int clearOrphans ()
  // ---------------------------------------------------------------------------------//
  {
    System.out.println ("Not implemented yet");
    return 0;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isSectorFree (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return freeBlocks.get (da.getBlockNo ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isSectorFree (int blockNo)
  // ---------------------------------------------------------------------------------//
  {
    return freeBlocks.get (blockNo);
  }

  // representation of the Free Sector Table
  // ---------------------------------------------------------------------------------//
  @Override
  public void setSectorFree (int block, boolean free)
  // ---------------------------------------------------------------------------------//
  {
    if (block < 0 || block >= freeBlocks.size ())
    {
      System.out.printf ("Block %d not in range : 0-%d%n", block, freeBlocks.size () - 1);
      return;
    }
    //assert block < freeBlocks.size () : String.format ("Set free block # %6d, size %6d",
    //                                                       block, freeBlocks.size ());
    freeBlocks.set (block, free);
  }

  // Check that the sector hasn't already been flagged as part of the disk structure
  // ---------------------------------------------------------------------------------//
  @Override
  public boolean stillAvailable (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return stillAvailable (da.getBlockNo ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean stillAvailable (int blockNo)
  // ---------------------------------------------------------------------------------//
  {
    return sectorTypes[blockNo] == usedSector || sectorTypes[blockNo] == emptySector;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void verify ()
  // ---------------------------------------------------------------------------------//
  {
    System.out.println ("Sectors to clean :");
    for (int i = 0, max = disk.getTotalBlocks (); i < max; i++)
    {
      if (freeBlocks.get (i))
      {
        if (sectorTypes[i] == usedSector)
          System.out.printf ("%04X clean%n", i);
      }
      else
      {
        if (sectorTypes[i] == usedSector)
          System.out.printf ("%04X *** error ***%n", i);
      }
    }
  }

  // VTOC flags sector as free, but it is in use by a file
  // ---------------------------------------------------------------------------------//
  @Override
  public int falsePositiveBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return falsePositives;
  }

  // VTOC flags sector as in use, but no file is using it (and not in the DOS tracks)
  // ---------------------------------------------------------------------------------//
  @Override
  public int falseNegativeBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return falseNegatives;
  }

  // ---------------------------------------------------------------------------------//
  public void addActionListener (ActionListener actionListener)
  // ---------------------------------------------------------------------------------//
  {
    actionListenerList = AWTEventMulticaster.add (actionListenerList, actionListener);
  }

  // ---------------------------------------------------------------------------------//
  public void removeActionListener (ActionListener actionListener)
  // ---------------------------------------------------------------------------------//
  {
    actionListenerList = AWTEventMulticaster.remove (actionListenerList, actionListener);
  }

  // ---------------------------------------------------------------------------------//
  public void notifyListeners (String text)
  // ---------------------------------------------------------------------------------//
  {
    if (actionListenerList != null)
      actionListenerList
          .actionPerformed (new ActionEvent (this, ActionEvent.ACTION_PERFORMED, text));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Disk name ............. %s%n", getDisplayPath ()));
    text.append (String.format ("Block size..... %d%n", disk.getBlockSize ()));
    text.append (String.format ("Interleave..... %d", disk.getInterleave ()));
    return text.toString ();
  }
}