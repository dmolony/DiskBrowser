package com.bytezone.diskbrowser.disk;

import java.awt.Dimension;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.gui.DataSource;

// Apple Assembly Lines disks are dual-dos
// Should be renamed MultiVolumeDisk (and allow >2 volumes)

// -----------------------------------------------------------------------------------//
public class HybridDisk implements FormattedDisk
// -----------------------------------------------------------------------------------//
{
  private final List<FormattedDisk> disks = new ArrayList<> (2);
  private int currentDisk;
  private final JTree tree;

  // ---------------------------------------------------------------------------------//
  public HybridDisk (FormattedDisk disk0, FormattedDisk disk1)
  // ---------------------------------------------------------------------------------//
  {
    assert disk0 != disk1;
    String diskName = disk0.getDisk ().getFile ().getName ();
    String text = "This disk is a hybrid of two or more OS\n\n" + disk0.getDisk ()
        + "\n\n" + disk1.getDisk ();

    DefaultAppleFileSource dafs = new DefaultAppleFileSource (diskName, text, this);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode (dafs);

    DefaultTreeModel treeModel = new DefaultTreeModel (root);
    tree = new JTree (treeModel);

    // allow empty nodes to appear as folders
    treeModel.setAsksAllowsChildren (true);

    disks.add (disk0);
    disks.add (disk1);

    disk0.setParent (this);
    disk1.setParent (this);

    DefaultMutableTreeNode root0 =
        (DefaultMutableTreeNode) disk0.getCatalogTree ().getModel ().getRoot ();
    DefaultMutableTreeNode root1 =
        (DefaultMutableTreeNode) disk1.getCatalogTree ().getModel ().getRoot ();

    root.add ((DefaultMutableTreeNode) root0.getChildAt (0));
    root.add ((DefaultMutableTreeNode) root1.getChildAt (0));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public JTree getCatalogTree ()
  // ---------------------------------------------------------------------------------//
  {
    return tree;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getFileSectors (fileNo);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<AppleFileSource> getCatalogList ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getCatalogList ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getFormattedSector (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getFormattedSector (da);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public SectorType getSectorType (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getSectorType (da);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public SectorType getSectorType (int track, int sector)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getSectorType (track, sector);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public SectorType getSectorType (int block)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getSectorType (block);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<SectorType> getSectorTypeList ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getSectorTypeList ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Disk getDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getDisk ();
  }

  // ---------------------------------------------------------------------------------//
  public void setCurrentDisk (FormattedDisk fd)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < disks.size (); i++)
      if (disks.get (i) == fd)
      {
        currentDisk = i;
        break;
      }

    //    if (disks[0] == fd)
    //      currentDisk = 0;
    //    else if (disks[1] == fd)
    //      currentDisk = 1;
    //    else
    //    {
    //      // this happens when the top-level folder is selected (i.e. neither disk)
    //      System.out.println ("Disk not found: " + fd);
    //      //      Utility.printStackTrace ();
    //    }
  }

  // ---------------------------------------------------------------------------------//
  public void setCurrentDiskNo (int n)
  // ---------------------------------------------------------------------------------//
  {
    currentDisk = n;
  }

  // ---------------------------------------------------------------------------------//
  public int getCurrentDiskNo ()
  // ---------------------------------------------------------------------------------//
  {
    return currentDisk;
  }

  // ---------------------------------------------------------------------------------//
  public FormattedDisk getCurrentDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void writeFile (AbstractFile file)
  // ---------------------------------------------------------------------------------//
  {
    disks.get (currentDisk).writeFile (file);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AppleFileSource getCatalog ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (FormattedDisk disk : disks)
    {
      text.append (disk.getCatalog ().getDataSource ().getText ());
      text.append ("\n\n");
    }

    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);

    return new DefaultAppleFileSource ("text", text.toString (), this);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AppleFileSource getFile (String uniqueName)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getFile (uniqueName);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int clearOrphans ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).clearOrphans ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isSectorFree (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).isSectorFree (da);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void verify ()
  // ---------------------------------------------------------------------------------//
  {
    disks.get (currentDisk).verify ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean stillAvailable (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).stillAvailable (da);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setSectorType (int block, SectorType type)
  // ---------------------------------------------------------------------------------//
  {
    disks.get (currentDisk).setSectorType (block, type);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setSectorFree (int block, boolean free)
  // ---------------------------------------------------------------------------------//
  {
    disks.get (currentDisk).setSectorFree (block, free);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int falseNegativeBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).falseNegativeBlocks ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int falsePositiveBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).falsePositiveBlocks ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Dimension getGridLayout ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getGridLayout ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isSectorFree (int block)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).isSectorFree (block);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean stillAvailable (int block)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).stillAvailable (block);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setOriginalPath (Path path)
  // ---------------------------------------------------------------------------------//
  {
    disks.get (currentDisk).setOriginalPath (path);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getAbsolutePath ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getAbsolutePath ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getDisplayPath ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getDisplayPath ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public FormattedDisk getParent ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getParent ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setParent (FormattedDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    disks.get (currentDisk).setParent (disk);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getSectorFilename (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getSectorFilename (da);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getName ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isTempDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).isTempDisk ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Path getOriginalPath ()
  // ---------------------------------------------------------------------------------//
  {
    return disks.get (currentDisk).getOriginalPath ();
  }
}