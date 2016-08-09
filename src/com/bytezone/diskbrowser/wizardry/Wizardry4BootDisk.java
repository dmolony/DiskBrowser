package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.bytezone.diskbrowser.applefile.Relocator;
import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.pascal.FileEntry;
import com.bytezone.diskbrowser.pascal.PascalDisk;
import com.bytezone.diskbrowser.utilities.Utility;

public class Wizardry4BootDisk extends PascalDisk
{
  List<AppleDisk> disks = new ArrayList<AppleDisk> ();
  protected Disk[] dataDisks;
  private Relocator relocator;

  public Wizardry4BootDisk (AppleDisk[] dataDisks)
  {
    super (dataDisks[0]);

    this.dataDisks = dataDisks;

    DefaultTreeModel model = (DefaultTreeModel) catalogTree.getModel ();
    DefaultMutableTreeNode currentRoot = (DefaultMutableTreeNode) model.getRoot ();
    DefaultMutableTreeNode relocNode = findNode (currentRoot, "SYSTEM.RELOC");
    FileEntry fileEntry = (FileEntry) relocNode.getUserObject ();

    if (fileEntry != null)
    {
      relocator =
          new Relocator (fileEntry.getUniqueName (), fileEntry.getDataSource ().buffer);
      relocator.createNewBuffer (dataDisks);
      fileEntry.setFile (relocator);
    }

    // reset the code segment so that it rebuilds itself from the new data
    DefaultMutableTreeNode pascalNode = findNode (currentRoot, "SYSTEM.PASCAL");
    fileEntry = (FileEntry) pascalNode.getUserObject ();
    fileEntry.setFile (null);
    fileEntry.getDataSource ();
  }

  public static boolean isWizardryIV (Disk disk, boolean debug)
  {
    byte[] header = { 0x00, (byte) 0xEA, (byte) 0xA9, 0x60, (byte) 0x8D, 0x01, 0x08 };
    byte[] buffer = disk.readSector (0);

    if (!Utility.matches (buffer, 0, header))
      return false;
    buffer = disk.readSector (1);

    if (buffer[510] != 1)
      return false;

    return true;
  }
}