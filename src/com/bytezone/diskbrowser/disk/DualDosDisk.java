package com.bytezone.diskbrowser.disk;

import java.awt.Dimension;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.gui.FileSelectedEvent;
import com.bytezone.diskbrowser.gui.FileSelectionListener;

// Apple Assembly Lines disks are dual-dos

public class DualDosDisk implements FormattedDisk, FileSelectionListener
{
  private final FormattedDisk[] disks = new FormattedDisk[2];
  private int currentDisk;
  private final JTree tree;

  public DualDosDisk (FormattedDisk disk0, FormattedDisk disk1)
  {
    String diskName = disk0.getDisk ().getFile ().getName ();
    String text = "This disk contains both DOS and Prodos files. Isn't that clever?\n\n"
        + disk0.getDisk () + "\n" + disk1.getDisk ();
    DefaultMutableTreeNode root =
        new DefaultMutableTreeNode (new DefaultAppleFileSource (diskName, text, this));
    DefaultTreeModel treeModel = new DefaultTreeModel (root);
    tree = new JTree (treeModel);
    treeModel.setAsksAllowsChildren (true); // allows empty nodes to appear as folders

    this.disks[0] = disk0;
    this.disks[1] = disk1;
    disk0.setParent (this);
    disk1.setParent (this);

    DefaultMutableTreeNode root0 =
        (DefaultMutableTreeNode) disk0.getCatalogTree ().getModel ().getRoot ();
    DefaultMutableTreeNode root1 =
        (DefaultMutableTreeNode) disk1.getCatalogTree ().getModel ().getRoot ();
    root.add ((DefaultMutableTreeNode) root0.getChildAt (0));
    root.add ((DefaultMutableTreeNode) root1.getChildAt (0));

    //    TreeNode[] nodes = ((DefaultTreeModel) tree.getModel ()).getPathToRoot (child);
    //    tree.setSelectionPath (new TreePath (nodes));
  }

  @Override
  public JTree getCatalogTree ()
  {
    return tree;
  }

  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  {
    return disks[currentDisk].getFileSectors (fileNo);
  }

  @Override
  public List<AppleFileSource> getCatalogList ()
  {
    return disks[currentDisk].getCatalogList ();
  }

  @Override
  public DataSource getFormattedSector (DiskAddress da)
  {
    return disks[currentDisk].getFormattedSector (da);
  }

  @Override
  public SectorType getSectorType (DiskAddress da)
  {
    return disks[currentDisk].getSectorType (da);
  }

  @Override
  public SectorType getSectorType (int track, int sector)
  {
    return disks[currentDisk].getSectorType (track, sector);
  }

  @Override
  public SectorType getSectorType (int block)
  {
    return disks[currentDisk].getSectorType (block);
  }

  @Override
  public List<SectorType> getSectorTypeList ()
  {
    return disks[currentDisk].getSectorTypeList ();
  }

  @Override
  public Disk getDisk ()
  {
    return disks[currentDisk].getDisk ();
  }

  public void setCurrentDisk (AppleFileSource afs)
  {
    FormattedDisk fd = afs.getFormattedDisk ();
    if (disks[0] == fd && currentDisk != 0)
      currentDisk = 0;
    else if (disks[1] == fd && currentDisk != 1)
      currentDisk = 1;

    //    System.out.println ("AFS : " + afs);
    //    System.out.println ("1. Setting current disk to : " + currentDisk);
  }

  public void setCurrentDiskNo (int n)
  {
    currentDisk = n;
    //    System.out.println ("2. Setting current disk to : " + currentDisk);
  }

  public int getCurrentDiskNo ()
  {
    return currentDisk;
  }

  public FormattedDisk getCurrentDisk ()
  {
    return disks[currentDisk];
  }

  @Override
  public void writeFile (AbstractFile file)
  {
    disks[currentDisk].writeFile (file);
  }

  @Override
  public AppleFileSource getCatalog ()
  {
    return new DefaultAppleFileSource ("text",
        disks[0].getCatalog ().getDataSource ().getText () + "\n\n"
            + disks[1].getCatalog ().getDataSource ().getText (),
        this);
  }

  @Override
  public AppleFileSource getFile (String uniqueName)
  {
    if (true)
      return disks[currentDisk].getFile (uniqueName);
    //    System.out.println ("Searching for : " + uniqueName);
    for (int i = 0; i < 2; i++)
    {
      AppleFileSource afs = disks[i].getFile (uniqueName);
      if (afs != null)
      {
        setCurrentDiskNo (i);
        return afs;
      }
    }
    return null;
  }

  @Override
  public int clearOrphans ()
  {
    return disks[currentDisk].clearOrphans ();
  }

  @Override
  public boolean isSectorFree (DiskAddress da)
  {
    return disks[currentDisk].isSectorFree (da);
  }

  @Override
  public void verify ()
  {
    disks[currentDisk].verify ();
  }

  @Override
  public boolean stillAvailable (DiskAddress da)
  {
    return disks[currentDisk].stillAvailable (da);
  }

  @Override
  public void setSectorType (int block, SectorType type)
  {
    disks[currentDisk].setSectorType (block, type);
  }

  @Override
  public void setSectorFree (int block, boolean free)
  {
    disks[currentDisk].setSectorFree (block, free);
  }

  @Override
  public int falseNegativeBlocks ()
  {
    return disks[currentDisk].falseNegativeBlocks ();
  }

  @Override
  public int falsePositiveBlocks ()
  {
    return disks[currentDisk].falsePositiveBlocks ();
  }

  @Override
  public Dimension getGridLayout ()
  {
    return disks[currentDisk].getGridLayout ();
  }

  @Override
  public boolean isSectorFree (int block)
  {
    return disks[currentDisk].isSectorFree (block);
  }

  @Override
  public boolean stillAvailable (int block)
  {
    return disks[currentDisk].stillAvailable (block);
  }

  @Override
  public void setOriginalPath (Path path)
  {
    disks[currentDisk].setOriginalPath (path);
  }

  @Override
  public String getAbsolutePath ()
  {
    return disks[currentDisk].getAbsolutePath ();
  }

  @Override
  public FormattedDisk getParent ()
  {
    return disks[currentDisk].getParent ();
  }

  @Override
  public void setParent (FormattedDisk disk)
  {
    disks[currentDisk].setParent (disk);
  }

  @Override
  public void fileSelected (FileSelectedEvent event)
  {
    System.out.println ("In DDD - file selected : " + event.file);
  }

  @Override
  public String getSectorFilename (DiskAddress da)
  {
    return disks[currentDisk].getSectorFilename (da);
  }

  @Override
  public String getName ()
  {
    return disks[currentDisk].getName ();
  }
}