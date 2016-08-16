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
import com.bytezone.diskbrowser.pascal.FileEntry;
import com.bytezone.diskbrowser.pascal.PascalDisk;
import com.bytezone.diskbrowser.utilities.Utility;
import com.bytezone.diskbrowser.wizardry.Header.ScenarioData;

public class Wizardry4BootDisk extends PascalDisk
{
  public Header scenarioHeader;
  List<AppleDisk> disks = new ArrayList<AppleDisk> ();
  //  protected Disk[] dataDisks;
  private Relocator relocator;
  private MessageBlock messageBlock;

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
      fileEntry.setFile (null);
      fileEntry.getDataSource ();
    }

    DefaultMutableTreeNode scenarioNode = findNode (currentRoot, "SCENARIO.DATA");
    fileEntry = (FileEntry) scenarioNode.getUserObject ();
    if (fileEntry != null)
    {
      fileEntry.setFile (null);

      scenarioNode.setAllowsChildren (true);
      scenarioHeader = new Header (scenarioNode, this);
      linkMazeLevels (scenarioNode, fileEntry);
    }

    DefaultMutableTreeNode monstersNode = findNode (currentRoot, "200.MONSTERS");
    fileEntry = (FileEntry) monstersNode.getUserObject ();
    if (fileEntry != null)
    {
      fileEntry.setFile (null);

      monstersNode.setAllowsChildren (true);
      linkMonsterImages (monstersNode, fileEntry);
    }

    DefaultMutableTreeNode messagesNode = findNode (currentRoot, "ASCII.KRN");
    fileEntry = (FileEntry) messagesNode.getUserObject ();
    if (fileEntry != null)
    {
      messageBlock = new MessageBlock (fileEntry.getDataSource ().buffer);
    }

    DefaultMutableTreeNode huffNode = findNode (currentRoot, "ASCII.HUFF");
    fileEntry = (FileEntry) huffNode.getUserObject ();
    if (fileEntry != null)
    {
      //      byte[] tree = new byte[256];
      //      byte[] left = new byte[256];
      //      byte[] right = new byte[256];

      byte[] buffer = fileEntry.getDataSource ().buffer;

      //      System.arraycopy (buffer, 0, tree, 0, 256);
      //      System.arraycopy (buffer, 256, left, 0, 256);
      //      System.arraycopy (buffer, 512, right, 0, 256);
      Huffman huffman = new Huffman (buffer);

      System.out.println (huffman.getMessage (messageBlock.getMessage (2043)));
      System.out.println (huffman.getMessage (messageBlock.getMessage (2044)));
      System.out.println (huffman.getMessage (messageBlock.getMessage (2045)));
    }
  }

  private void linkMazeLevels (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  {
    ScenarioData mazeData = scenarioHeader.data.get (Header.MAZE_AREA);

    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    DefaultMutableTreeNode mazeNode = linkNode ("Maze", "Levels string", scenarioNode);
    for (int i = 0; i < 15; i++)
    {
      byte[] level = new byte[896];
      System.arraycopy (buffer, mazeData.dataOffset * 512 + i * 1024, level, 0,
          level.length);

      List<DiskAddress> mazeBlocks = new ArrayList<DiskAddress> ();
      int ptr = mazeData.dataOffset + i * 2;
      mazeBlocks.add (blocks.get (ptr));
      mazeBlocks.add (blocks.get (ptr + 1));
      addToNode (new MazeLevel (level, i), mazeNode, mazeBlocks);
    }
  }

  private void linkMonsterImages (DefaultMutableTreeNode monstersNode,
      FileEntry fileEntry)
  {
    byte[] pictureBuffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> pictureBlocks = fileEntry.getSectors ();

    int count = 0;
    for (int block = 0; block < 24; block++)
    {
      int ptr = block * 512;
      for (int pic = 0; pic < 2; pic++)
      {
        byte[] buffer = new byte[240];
        System.arraycopy (pictureBuffer, ptr + pic * 256, buffer, 0, 240);
        Wiz4Image image = new Wiz4Image ("Image " + count++, buffer);
        List<DiskAddress> monsterBlocks = new ArrayList<DiskAddress> ();
        monsterBlocks.add (pictureBlocks.get (block));
        addToNode (image, monstersNode, monsterBlocks);
      }
    }
  }

  private void addToNode (AbstractFile af, DefaultMutableTreeNode node,
      List<DiskAddress> blocks)
  {
    DefaultAppleFileSource dafs =
        new DefaultAppleFileSource (af.getName (), af, this, blocks);
    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode (dafs);
    node.add (childNode);
    childNode.setAllowsChildren (false);
  }

  private DefaultMutableTreeNode linkNode (String name, String text,
      DefaultMutableTreeNode parent)
  {
    DefaultAppleFileSource afs = new DefaultAppleFileSource (name, text, this);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode (afs);
    parent.add (node);
    return node;
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