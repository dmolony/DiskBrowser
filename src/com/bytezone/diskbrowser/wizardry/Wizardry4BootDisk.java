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
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;
import com.bytezone.diskbrowser.wizardry.Header.ScenarioData;

public class Wizardry4BootDisk extends PascalDisk
{
  public Header scenarioHeader;
  //  private final List<AppleDisk> disks = new ArrayList<AppleDisk> ();
  private Relocator relocator;
  private MessageBlock messageBlock;
  private Huffman huffman;
  private final int version;

  public Wizardry4BootDisk (AppleDisk[] dataDisks)
  {
    super (dataDisks[0]);

    version = dataDisks.length == 6 ? 4 : dataDisks.length == 10 ? 5 : 0;

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

    DefaultMutableTreeNode huffNode = findNode (currentRoot, "ASCII.HUFF");
    fileEntry = (FileEntry) huffNode.getUserObject ();
    if (fileEntry != null)
    {

      byte[] buffer = fileEntry.getDataSource ().buffer;

      huffman = new Huffman ("Huffman tree", buffer);
      fileEntry.setFile (huffman);
    }

    DefaultMutableTreeNode messagesNode = findNode (currentRoot, "ASCII.KRN");
    fileEntry = (FileEntry) messagesNode.getUserObject ();
    if (fileEntry != null)
    {
      messageBlock = new MessageBlock (fileEntry.getDataSource ().buffer, huffman);
      fileEntry.setFile (messageBlock);
      messagesNode.setAllowsChildren (true);
      List<DiskAddress> blocks = fileEntry.getSectors ();

      int count = 0;
      for (MessageDataBlock mdb : messageBlock)
      {
        List<DiskAddress> messageBlocks = new ArrayList<DiskAddress> ();
        messageBlocks.add (blocks.get (count++));
        addToNode (mdb, messagesNode, messageBlocks);
      }
    }

    if (version == 4)
    {
      DefaultMutableTreeNode scenarioNode = findNode (currentRoot, "SCENARIO.DATA");
      fileEntry = (FileEntry) scenarioNode.getUserObject ();
      if (fileEntry != null)
      {
        fileEntry.setFile (null);
        scenarioNode.setAllowsChildren (true);
        scenarioHeader = new Header (scenarioNode, this);
        linkMazeLevels4 (scenarioNode, fileEntry);
      }
    }
    else if (version == 5)
    {
      DefaultMutableTreeNode scenarioNode = findNode (currentRoot, "DRAGON.DATA");
      fileEntry = (FileEntry) scenarioNode.getUserObject ();
      if (fileEntry != null)
      {
        fileEntry.setFile (null);
        scenarioNode.setAllowsChildren (true);
        linkMazeLevels5 (scenarioNode, fileEntry);
        linkBlock1 (scenarioNode, fileEntry);
        linkOracle (scenarioNode, fileEntry);
        linkBlock2 (scenarioNode, fileEntry);
      }
    }

    if (version == 4)
    {
      DefaultMutableTreeNode monstersNode = findNode (currentRoot, "200.MONSTERS");
      fileEntry = (FileEntry) monstersNode.getUserObject ();
      if (fileEntry != null)
      {
        monstersNode.setAllowsChildren (true);
        linkMonsterImages4 (monstersNode, fileEntry);
      }
    }
    else if (version == 5)
    {
      DefaultMutableTreeNode monstersNode = findNode (currentRoot, "200.MONSTERS");
      fileEntry = (FileEntry) monstersNode.getUserObject ();
      if (fileEntry != null)
      {
        monstersNode.setAllowsChildren (true);
        linkMonsterImages5 (monstersNode, fileEntry);
      }
    }
  }

  private void linkMonsterImages4 (DefaultMutableTreeNode monstersNode,
      FileEntry fileEntry)
  {
    List<DiskAddress> pictureBlocks = fileEntry.getSectors ();

    Wiz4Monsters w4monsters =
        new Wiz4Monsters ("monsters", fileEntry.getDataSource ().buffer);
    fileEntry.setFile (w4monsters);

    int count = 0;
    for (Wiz4Image image : w4monsters.images)
    {
      List<DiskAddress> monsterBlocks = new ArrayList<DiskAddress> ();
      monsterBlocks.add (pictureBlocks.get (w4monsters.blocks.get (count++)));
      addToNode (image, monstersNode, monsterBlocks);
    }
  }

  private void linkMonsterImages5 (DefaultMutableTreeNode monstersNode,
      FileEntry fileEntry)
  {
    List<DiskAddress> pictureBlocks = fileEntry.getSectors ();

    Wiz5Monsters w5monsters =
        new Wiz5Monsters ("monsters", fileEntry.getDataSource ().buffer);
    fileEntry.setFile (w5monsters);

    for (Wiz5Monsters.Monster monster : w5monsters)
    {
      List<DiskAddress> monsterBlocks = new ArrayList<DiskAddress> ();
      for (Integer blockId : monster.getBlocks ())
        monsterBlocks.add (pictureBlocks.get (blockId));
      addToNode (monster.getImage (), monstersNode, monsterBlocks);
    }
  }

  private void linkMazeLevels4 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  {
    ScenarioData mazeData = scenarioHeader.data.get (Header.MAZE_AREA);

    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    DefaultMutableTreeNode mazeNode = linkNode ("Maze", "Levels string", scenarioNode);
    for (int i = 0; i < 15; i++)
    {
      byte[] level = new byte[0x380];           // 896
      int offset = mazeData.dataOffset * 512 + i * 1024;
      System.arraycopy (buffer, offset, level, 0, level.length);

      List<DiskAddress> mazeBlocks = new ArrayList<DiskAddress> ();
      int ptr = mazeData.dataOffset + i * 2;
      mazeBlocks.add (blocks.get (ptr));
      mazeBlocks.add (blocks.get (ptr + 1));
      addToNode (new MazeLevel (level, i), mazeNode, mazeBlocks);
    }
  }

  private void linkMazeLevels5 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  {
    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    DefaultMutableTreeNode mazeNode = linkNode ("Maze", "Level 5 mazes", scenarioNode);
    List<DiskAddress> allMazeBlocks = new ArrayList<DiskAddress> ();

    int dataSize = 0x39A;
    int base = 0x1800;
    for (int i = 0; i < 8; i++)
    {
      int offset = base + i * 0x400;
      byte[] data = new byte[0x800];
      System.arraycopy (buffer, offset, data, 0, dataSize);
      System.arraycopy (buffer, offset + 0x2000, data, 0x400, dataSize);
      MazeGridV5 grid = new MazeGridV5 ("Maze level " + (i + 1), data, messageBlock);

      List<DiskAddress> mazeBlocks = new ArrayList<DiskAddress> ();
      for (int j = 0; j < 4; j++)
        mazeBlocks.add (blocks.get (12 + i * 4 + j));
      allMazeBlocks.addAll (mazeBlocks);

      addToNode (grid, mazeNode, mazeBlocks);
    }

    DefaultAppleFileSource afs = (DefaultAppleFileSource) mazeNode.getUserObject ();
    afs.setSectors (allMazeBlocks);
  }

  private void linkBlock1 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  {
    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    StringBuilder text = new StringBuilder ();
    List<DiskAddress> allBlocks = new ArrayList<DiskAddress> ();
    for (int i = 0; i < 23; i++)
    {
      allBlocks.add (blocks.get (44 + i));
    }

    int offset = 0x5800;
    int length = 66;
    for (int i = 0; i < 179; i++)
    {
      text.append (String.format ("%04X : %s%n", (offset + i * length),
          HexFormatter.getHexString (buffer, offset + i * length, length)));
    }

    DefaultMutableTreeNode oracleNode =
        linkNode ("Block1", text.toString (), scenarioNode);
    oracleNode.setAllowsChildren (false);
    DefaultAppleFileSource afs = (DefaultAppleFileSource) oracleNode.getUserObject ();
    afs.setSectors (allBlocks);
  }

  private void linkBlock2 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  {
    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    StringBuilder text = new StringBuilder ();
    List<DiskAddress> allBlocks = new ArrayList<DiskAddress> ();
    for (int i = 0; i < 19; i++)
    {
      allBlocks.add (blocks.get (87 + i));
    }

    int offset = 0xAE00;
    int length = 60;
    for (int i = 0; i < 150; i++)
    {
      text.append (String.format ("%04X : %s%n", (offset + i * length),
          HexFormatter.getHexString (buffer, offset + i * length, length)));
    }

    DefaultMutableTreeNode oracleNode =
        linkNode ("Block2", text.toString (), scenarioNode);
    oracleNode.setAllowsChildren (false);
    DefaultAppleFileSource afs = (DefaultAppleFileSource) oracleNode.getUserObject ();
    afs.setSectors (allBlocks);
  }

  private void linkOracle (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  {
    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < 320; i++)
    {
      //      System.out.println (HexFormatter.format (buffer, 0x08600 + i * 32, 32));
      int offset = 0x08600 + i * 32 + 18;
      int key = HexFormatter.getShort (buffer, offset);
      if (key > 0)
        text.append (String.format ("%04X  %04X  * %s%n", offset, key,
            messageBlock.getMessageText (key)));
      key = HexFormatter.getShort (buffer, offset + 8);
      if (key > 0)
        text.append (String.format ("%04X  %04X    %s%n", offset + 8, key,
            messageBlock.getMessageText (key)));
    }

    List<DiskAddress> allOracleBlocks = new ArrayList<DiskAddress> ();
    for (int i = 0; i < 20; i++)
    {
      allOracleBlocks.add (blocks.get (67 + i));
    }

    DefaultMutableTreeNode oracleNode =
        linkNode ("Oracle", text.toString (), scenarioNode);
    oracleNode.setAllowsChildren (false);
    DefaultAppleFileSource afs = (DefaultAppleFileSource) oracleNode.getUserObject ();
    afs.setSectors (allOracleBlocks);
  }

  private void addToNode (AbstractFile af, DefaultMutableTreeNode node,
      List<DiskAddress> blocks)
  {
    DefaultAppleFileSource dafs =
        new DefaultAppleFileSource (af.getName (), af, this, blocks);
    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode (dafs);
    childNode.setAllowsChildren (false);
    node.add (childNode);
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