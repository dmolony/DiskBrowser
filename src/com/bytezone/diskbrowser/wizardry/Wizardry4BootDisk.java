package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.SectorType;
import com.bytezone.diskbrowser.pascal.FileEntry;
import com.bytezone.diskbrowser.pascal.PascalDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

public class Wizardry4BootDisk extends PascalDisk
{
  List<AppleDisk> disks = new ArrayList<AppleDisk> ();
  //  protected Disk[] dataDisks;
  private Relocator relocator;

  public Wizardry4BootDisk (AppleDisk[] dataDisks)
  {
    super (dataDisks[0]);

    //    this.dataDisks = dataDisks;

    DefaultTreeModel model = (DefaultTreeModel) catalogTree.getModel ();
    DefaultMutableTreeNode currentRoot = (DefaultMutableTreeNode) model.getRoot ();

    // get the relocation table
    DefaultMutableTreeNode relocNode = findNode (currentRoot, "SYSTEM.RELOC");
    FileEntry fileEntry = (FileEntry) relocNode.getUserObject ();

    if (fileEntry != null)
    {
      relocator =
          new Relocator (fileEntry.getUniqueName (), fileEntry.getDataSource ().buffer);
      relocator.createNewBuffer (dataDisks);    // create new data buffer
      fileEntry.setFile (relocator);
    }

    // reset the code segment so that it rebuilds itself from the new data
    DefaultMutableTreeNode pascalNode = findNode (currentRoot, "SYSTEM.PASCAL");
    fileEntry = (FileEntry) pascalNode.getUserObject ();

    if (fileEntry != null)
    {
      //      fileEntry.setFile (null);
      //      fileEntry.getDataSource ();
    }

    DefaultMutableTreeNode scenarioNode = findNode (currentRoot, "SCENARIO.DATA");
    fileEntry = (FileEntry) scenarioNode.getUserObject ();

    if (fileEntry != null)
    {
      fileEntry.setFile (null);
      scenarioNode.setAllowsChildren (true);

      fileEntry.setFile (null);
      byte[] buffer = fileEntry.getDataSource ().buffer;

      for (int i = 0; i < 11; i++)
      {
        byte[] level = new byte[896];
        System.out.println (HexFormatter.format (buffer, 0, 512));
        System.arraycopy (buffer, 0xC600 + i * 1024, level, 0, level.length);
        MazeLevel maze = new MazeLevel (level, i);

        List<DiskAddress> blocks = new ArrayList<DiskAddress> ();
        addToNode (maze, scenarioNode, blocks, null);
      }
    }

    DefaultMutableTreeNode monstersNode = findNode (currentRoot, "200.MONSTERS");
    fileEntry = (FileEntry) monstersNode.getUserObject ();

    if (fileEntry != null)
    {
      monstersNode.setAllowsChildren (true);
      byte[] pictureBuffer = fileEntry.getDataSource ().buffer;
      List<DiskAddress> pictureBlocks = fileEntry.getSectors ();

      int count = 0;
      loop: for (int block = 0; block < 24; block++)
      {
        int ptr = block * 512;
        for (int pic = 0; pic < 2; pic++)
        {
          byte[] buffer = new byte[240];
          System.arraycopy (pictureBuffer, ptr + pic * 256, buffer, 0, 240);
          Wiz4Image image = new Wiz4Image ("Image " + count++, buffer);
          List<DiskAddress> blocks = new ArrayList<DiskAddress> ();
          blocks.add (pictureBlocks.get (block));
          addToNode (image, monstersNode, blocks, null);
        }
      }
    }
  }

  private void addToNode (AbstractFile af, DefaultMutableTreeNode node,
      List<DiskAddress> blocks, SectorType type)
  {
    DefaultAppleFileSource dafs =
        new DefaultAppleFileSource (af.getName (), af, this, blocks);
    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode (dafs);
    node.add (childNode);
    childNode.setAllowsChildren (false);
  }

  public static boolean isWizardryIVorV (Disk disk, boolean debug)
  {
    // Wizardry IV or V boot code
    byte[] header = { 0x00, (byte) 0xEA, (byte) 0xA9, 0x60, (byte) 0x8D, 0x01, 0x08 };
    byte[] buffer = disk.readSector (0);

    if (!Utility.matches (buffer, 0, header))
      return false;

    buffer = disk.readSector (1);
    if (buffer[510] != 1 || buffer[511] != 0)       // disk #1
      return false;

    return true;
  }
}