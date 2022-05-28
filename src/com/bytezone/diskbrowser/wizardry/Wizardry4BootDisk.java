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

// -----------------------------------------------------------------------------------//
public class Wizardry4BootDisk extends PascalDisk
// -----------------------------------------------------------------------------------//
{
  public Header scenarioHeader;
  private Relocator relocator;
  private MessageBlock messageBlock;
  private Huffman huffman;
  private final int version;

  // ---------------------------------------------------------------------------------//
  public Wizardry4BootDisk (AppleDisk[] dataDisks)
  // ---------------------------------------------------------------------------------//
  {
    super (dataDisks[0]);

    version = dataDisks.length == 7 ? 4 : dataDisks.length == 10 ? 5 : 0;

    DefaultTreeModel model = (DefaultTreeModel) catalogTree.getModel ();
    DefaultMutableTreeNode currentRoot = (DefaultMutableTreeNode) model.getRoot ();

    // get the relocation table
    DefaultMutableTreeNode relocNode = findNode (currentRoot, "SYSTEM.RELOC");
    FileEntry fileEntry = (FileEntry) relocNode.getUserObject ();
    if (fileEntry != null)
    {
      relocator = new Relocator (fileEntry.getUniqueName (), fileEntry.getDataSource ().buffer);
      relocator.createNewBuffer (dataDisks);
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
      huffman = new Huffman ("Huffman tree", fileEntry.getDataSource ().buffer);
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
        List<DiskAddress> messageBlocks = new ArrayList<> ();
        messageBlocks.add (blocks.get (count++));
        addToNode (mdb, messagesNode, messageBlocks);
      }
    }

    // scenario data
    if (version == 4)
    {
      DefaultMutableTreeNode scenarioNode = findNode (currentRoot, "SCENARIO.DATA");
      fileEntry = (FileEntry) scenarioNode.getUserObject ();
      if (fileEntry != null)
      {
        fileEntry.setFile (null);
        scenarioNode.setAllowsChildren (true);
        scenarioHeader = new Header (scenarioNode, this);
        linkCharacters4 (scenarioNode, fileEntry);
        linkMazeLevels4 (scenarioNode, fileEntry);
        linkMonstersV4 (scenarioNode, fileEntry);
        linkItemsV4 (scenarioNode, fileEntry);
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
    else
      System.out.println ("No Wizardry version set");

    // monster images
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

  // ---------------------------------------------------------------------------------//
  private void linkCharacters4 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    ScenarioData sd = scenarioHeader.get (Header.CHARACTER_AREA);

    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    DefaultMutableTreeNode charactersNode = linkNode ("Characters", "Characters", scenarioNode);
    List<DiskAddress> allCharacterBlocks = new ArrayList<> ();

    int ptr = sd.dataOffset * 512;

    for (int i = 0; i < 500; i++)
    {
      byte[] out = huffman.decodeMessage (buffer, ptr, sd.totalBlocks);

      String name = HexFormatter.getPascalString (out, 1);

      Character4 c = new Character4 (name, out);
      List<DiskAddress> characterBlocks = new ArrayList<> ();
      DiskAddress da = blocks.get (ptr / 512);
      characterBlocks.add (da);
      addToNode (c, charactersNode, characterBlocks);

      if (!allCharacterBlocks.contains (da))
        allCharacterBlocks.add (da);

      ptr += sd.totalBlocks;
    }

    DefaultAppleFileSource afs = (DefaultAppleFileSource) charactersNode.getUserObject ();
    afs.setSectors (allCharacterBlocks);
  }

  // ---------------------------------------------------------------------------------//
  private void linkMonstersV4 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    ScenarioData sd = scenarioHeader.get (Header.MONSTER_AREA);

    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    DefaultMutableTreeNode monstersNode = linkNode ("Monsters", "Monsters", scenarioNode);
    List<DiskAddress> allMonsterBlocks = new ArrayList<> ();

    String[] monsterNames = new String[4];

    int ptr = sd.dataOffset * 512;

    for (int i = 0; i < sd.total; i++)
    {
      byte[] out = huffman.decodeMessage (buffer, ptr, sd.totalBlocks);

      for (int j = 0; j < 4; j++)
        monsterNames[j] = messageBlock.getMessageLine (i * 4 + 13000 + j);

      MonsterV4 monster = new MonsterV4 (monsterNames, out, i);

      List<DiskAddress> monsterBlocks = new ArrayList<> ();
      DiskAddress da = blocks.get (ptr / 512);
      monsterBlocks.add (da);
      addToNode (monster, monstersNode, monsterBlocks);

      if (!allMonsterBlocks.contains (da))
        allMonsterBlocks.add (da);

      ptr += sd.totalBlocks;
    }

    DefaultAppleFileSource afs = (DefaultAppleFileSource) monstersNode.getUserObject ();
    afs.setSectors (allMonsterBlocks);
  }

  // ---------------------------------------------------------------------------------//
  private void linkItemsV4 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    ScenarioData sd = scenarioHeader.get (Header.ITEM_AREA);

    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    DefaultMutableTreeNode itemsNode = linkNode ("Items", "Items", scenarioNode);
    List<DiskAddress> allItemBlocks = new ArrayList<> ();

    String[] itemNames = new String[2];

    int ptr = sd.dataOffset * 512;

    for (int i = 0; i < sd.total; i++)
    {
      byte[] out = huffman.decodeMessage (buffer, ptr, sd.totalBlocks);

      for (int j = 0; j < 2; j++)
      {
        itemNames[j] = messageBlock.getMessageLine (i * 2 + 14000 + j);
        if (itemNames[j] == null)
          itemNames[j] = "Not found";
      }

      ItemV4 item = new ItemV4 (itemNames, out, i);

      List<DiskAddress> itemBlocks = new ArrayList<> ();
      DiskAddress da = blocks.get (ptr / 512);
      itemBlocks.add (da);
      addToNode (item, itemsNode, itemBlocks);

      if (!allItemBlocks.contains (da))
        allItemBlocks.add (da);

      ptr += sd.totalBlocks;
    }

    DefaultAppleFileSource afs = (DefaultAppleFileSource) itemsNode.getUserObject ();
    afs.setSectors (allItemBlocks);
  }

  // ---------------------------------------------------------------------------------//
  private void linkMazeLevels4 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    ScenarioData mazeData = scenarioHeader.get (Header.MAZE_AREA);

    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    DefaultMutableTreeNode mazeNode = linkNode ("Maze", "Levels string", scenarioNode);
    List<DiskAddress> allMazeBlocks = new ArrayList<> ();

    for (int i = 0; i < mazeData.total; i++)
    {
      int blockPtr = mazeData.dataOffset + i * 2;

      byte[] level = new byte[0x380];           // 896
      System.arraycopy (buffer, blockPtr * 512, level, 0, level.length);

      List<DiskAddress> mazeBlocks = new ArrayList<> ();
      mazeBlocks.add (blocks.get (blockPtr));
      mazeBlocks.add (blocks.get (blockPtr + 1));
      addToNode (new MazeLevel (level, i), mazeNode, mazeBlocks);
      allMazeBlocks.addAll (mazeBlocks);
    }

    DefaultAppleFileSource afs = (DefaultAppleFileSource) mazeNode.getUserObject ();
    afs.setSectors (allMazeBlocks);
  }

  // ---------------------------------------------------------------------------------//
  private void linkMonsterImages4 (DefaultMutableTreeNode monstersNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> pictureBlocks = fileEntry.getSectors ();

    Wiz4Monsters w4monsters = new Wiz4Monsters ("monsters", fileEntry.getDataSource ().buffer);
    fileEntry.setFile (w4monsters);

    int count = 0;
    for (Wiz4Image image : w4monsters.images)
    {
      List<DiskAddress> monsterBlocks = new ArrayList<> ();
      monsterBlocks.add (pictureBlocks.get (w4monsters.blocks.get (count++)));
      addToNode (image, monstersNode, monsterBlocks);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void linkMonsterImages5 (DefaultMutableTreeNode monstersNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> pictureBlocks = fileEntry.getSectors ();

    Wiz5Monsters w5monsters = new Wiz5Monsters ("monsters", fileEntry.getDataSource ().buffer);
    fileEntry.setFile (w5monsters);

    for (Wiz5Monsters.Monster monster : w5monsters)
    {
      List<DiskAddress> monsterBlocks = new ArrayList<> ();
      for (Integer blockId : monster.getBlocks ())
        monsterBlocks.add (pictureBlocks.get (blockId));
      addToNode (monster.getImage (), monstersNode, monsterBlocks);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void linkMazeLevels5 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    DefaultMutableTreeNode mazeNode = linkNode ("Maze", "Level 5 mazes", scenarioNode);
    List<DiskAddress> allMazeBlocks = new ArrayList<> ();

    int dataSize = 0x39A;
    int base = 0x1800;
    for (int i = 0; i < 8; i++)
    {
      int offset = base + i * 0x400;
      byte[] data = new byte[0x800];
      System.arraycopy (buffer, offset, data, 0, dataSize);
      System.arraycopy (buffer, offset + 0x2000, data, 0x400, dataSize);
      MazeGridV5 grid = new MazeGridV5 ("Maze level " + (i + 1), data, messageBlock);

      List<DiskAddress> mazeBlocks = new ArrayList<> ();
      for (int j = 0; j < 4; j++)
        mazeBlocks.add (blocks.get (12 + i * 4 + j));
      allMazeBlocks.addAll (mazeBlocks);

      addToNode (grid, mazeNode, mazeBlocks);
    }

    DefaultAppleFileSource afs = (DefaultAppleFileSource) mazeNode.getUserObject ();
    afs.setSectors (allMazeBlocks);
  }

  // ---------------------------------------------------------------------------------//
  private void linkBlock1 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    StringBuilder text = new StringBuilder ();
    List<DiskAddress> allBlocks = new ArrayList<> ();
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

    DefaultMutableTreeNode oracleNode = linkNode ("Block1", text.toString (), scenarioNode);
    oracleNode.setAllowsChildren (false);
    DefaultAppleFileSource afs = (DefaultAppleFileSource) oracleNode.getUserObject ();
    afs.setSectors (allBlocks);
  }

  // ---------------------------------------------------------------------------------//
  private void linkBlock2 (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    StringBuilder text = new StringBuilder ();
    List<DiskAddress> allBlocks = new ArrayList<> ();
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

    DefaultMutableTreeNode oracleNode = linkNode ("Block2", text.toString (), scenarioNode);
    oracleNode.setAllowsChildren (false);
    DefaultAppleFileSource afs = (DefaultAppleFileSource) oracleNode.getUserObject ();
    afs.setSectors (allBlocks);
  }

  // ---------------------------------------------------------------------------------//
  private void linkOracle (DefaultMutableTreeNode scenarioNode, FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = fileEntry.getDataSource ().buffer;
    List<DiskAddress> blocks = fileEntry.getSectors ();

    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < 320; i++)
    {
      //      System.out.println (HexFormatter.format (buffer, 0x08600 + i * 32, 32));
      int offset = 0x08600 + i * 32 + 18;
      int key = Utility.getShort (buffer, offset);
      if (key > 0)
        text.append (
            String.format ("%04X  %04X  * %s%n", offset, key, messageBlock.getMessageLine (key)));
      key = Utility.getShort (buffer, offset + 8);
      if (key > 0)
        text.append (String.format ("%04X  %04X    %s%n", offset + 8, key,
            messageBlock.getMessageLine (key)));
    }

    List<DiskAddress> allOracleBlocks = new ArrayList<> ();
    for (int i = 0; i < 20; i++)
    {
      allOracleBlocks.add (blocks.get (67 + i));
    }

    DefaultMutableTreeNode oracleNode = linkNode ("Oracle", text.toString (), scenarioNode);
    oracleNode.setAllowsChildren (false);
    DefaultAppleFileSource afs = (DefaultAppleFileSource) oracleNode.getUserObject ();
    afs.setSectors (allOracleBlocks);
  }

  // ---------------------------------------------------------------------------------//
  private void addToNode (AbstractFile af, DefaultMutableTreeNode node, List<DiskAddress> blocks)
  // ---------------------------------------------------------------------------------//
  {
    DefaultAppleFileSource dafs = new DefaultAppleFileSource (af.getName (), af, this, blocks);
    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode (dafs);
    childNode.setAllowsChildren (false);
    node.add (childNode);
  }

  // ---------------------------------------------------------------------------------//
  private DefaultMutableTreeNode linkNode (String name, String text, DefaultMutableTreeNode parent)
  // ---------------------------------------------------------------------------------//
  {
    DefaultAppleFileSource afs = new DefaultAppleFileSource (name, text, this);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode (afs);
    parent.add (node);

    return node;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isWizardryIVorV (Disk disk, boolean debug)
  // ---------------------------------------------------------------------------------//
  {
    // Wizardry IV or V boot code
    byte[] header = { 0x00, (byte) 0xEA, (byte) 0xA9, 0x60, (byte) 0x8D, 0x01, 0x08 };
    byte[] buffer = disk.readBlock (0);

    if (!Utility.matches (buffer, 0, header))
      return false;

    buffer = disk.readBlock (1);
    return buffer[510] == 1 && buffer[511] == 0;          // disk #1
  }
}