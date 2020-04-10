package com.bytezone.diskbrowser.wizardry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.DefaultDataSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.SectorType;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.pascal.PascalDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.wizardry.Character.Attributes;
import com.bytezone.diskbrowser.wizardry.Character.Statistics;
import com.bytezone.diskbrowser.wizardry.Header.ScenarioData;
import com.bytezone.diskbrowser.wizardry.Spell.SpellType;

// ---------------------------------------------------------------------------------//
public class WizardryScenarioDisk extends PascalDisk
// ---------------------------------------------------------------------------------//
{
  public Header scenarioHeader;

  public List<AbstractImage> images;
  public List<Item> items;
  public List<Character> characters;
  public List<Spell> spells;
  public List<Message> messages;
  public List<Monster> monsters;
  public List<MazeLevel> levels;
  List<ExperienceLevel> experiences;
  List<Reward> rewards;

  // leave these here until I decide whether to use them or not
  SectorType mazeSector = new SectorType ("Maze", Color.lightGray);
  SectorType monsterSector = new SectorType ("Monsters", Color.black);
  SectorType itemSector = new SectorType ("Items", Color.blue);
  SectorType characterSector = new SectorType ("Characters", Color.magenta);
  SectorType spellSector = new SectorType ("Spells", Color.orange);
  SectorType messageSector = new SectorType ("Messages", Color.cyan);
  SectorType imageSector = new SectorType ("Images", Color.red);
  SectorType experienceSector = new SectorType ("Experience", Color.darkGray);
  SectorType treasureSector = new SectorType ("Treasure", Color.pink);

  // ---------------------------------------------------------------------------------//
  public WizardryScenarioDisk (Disk disk)
  // ---------------------------------------------------------------------------------//
  {
    super (disk);

    if (false)
    {
      sectorTypesList.add (mazeSector);
      sectorTypesList.add (monsterSector);
      sectorTypesList.add (itemSector);
      sectorTypesList.add (characterSector);
      sectorTypesList.add (spellSector);
      sectorTypesList.add (messageSector);
      sectorTypesList.add (imageSector);
      sectorTypesList.add (experienceSector);
      sectorTypesList.add (treasureSector);
    }

    CodedMessage.codeOffset = 185;
    Monster.counter = 0;
    Item.counter = 0;

    DefaultTreeModel model = (DefaultTreeModel) catalogTree.getModel ();
    DefaultMutableTreeNode currentRoot = (DefaultMutableTreeNode) model.getRoot ();
    DefaultMutableTreeNode dataNode = findNode (currentRoot, "SCENARIO.DATA");
    DefaultMutableTreeNode msgNode = findNode (currentRoot, "SCENARIO.MESGS");
    if (dataNode == null || msgNode == null)
    {
      System.out.println ("Wizardry data or msg node not found");
      return;
    }
    dataNode.setAllowsChildren (true);
    msgNode.setAllowsChildren (true);

    scenarioHeader = new Header (dataNode, this);

    // Process SCENARIO.MESGS (requires scenario)
    AppleFileSource afs = (AppleFileSource) msgNode.getUserObject ();
    //    DefaultMutableTreeNode node = linkNode ("Messages", "Messages string", msgNode);
    extractMessages (msgNode, afs.getSectors ());
    //		makeNodeVisible (node);

    // Process SCENARIO.DATA (requires scenario and messages)
    afs = (AppleFileSource) dataNode.getUserObject ();
    List<DiskAddress> sectors = afs.getSectors ();

    extractItems (linkNode ("Items", "Items string", dataNode), sectors);
    extractRewards (linkNode ("Rewards", "Treasure string", dataNode), sectors);
    extractMonsters (linkNode ("Monsters", "Monsters string", dataNode), sectors);
    extractCharacters (linkNode ("Characters", "Characters string", dataNode), sectors);
    extractImages (linkNode ("Images", "Images string", dataNode), sectors);
    extractExperienceLevels (linkNode ("Experience", "Experience string", dataNode),
        sectors);
    //		node = linkNode ("Spells", "Spells string", dataNode);
    DefaultMutableTreeNode node = null;
    extractSpells (node, sectors);
    extractLevels (linkNode ("Maze", "Levels string", dataNode), sectors);
    // Make the Spells node (and its siblings) visible
    //		makeNodeVisible (node);

    // add information about each characters' baggage, spells known etc.
    for (Character c : characters)
    {
      c.linkItems (items);
      c.linkSpells (spells);
      int type = c.getStatistics ().typeInt;
      c.linkExperience (experiences.get (type));
    }
  }

  // ---------------------------------------------------------------------------------//
  private DefaultMutableTreeNode linkNode (String name, String text,
      DefaultMutableTreeNode parent)
  // ---------------------------------------------------------------------------------//
  {
    DefaultAppleFileSource afs = new DefaultAppleFileSource (name, text, this);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode (afs);
    parent.add (node);
    return node;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isWizardryFormat (Disk disk, boolean debug)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = disk.readBlock (2);
    int totalFiles = HexFormatter.intValue (buffer[16], buffer[17]);
    if (totalFiles != 3)
      return false;

    for (int i = 1, ptr = 32; i <= totalFiles; i++, ptr += 26)
    {
      String text = HexFormatter.getPascalString (buffer, ptr);
      if (!text.equals ("SCENARIO.DATA") && !text.equals ("SCENARIO.MESGS")
          && !text.equals ("WIZARDRY.CODE"))
        return false;
    }
    return true;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AppleFileSource getFile (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    //    System.out.println ("Wizardry disk looking for : " + fileName);
    return null;
  }

  // ---------------------------------------------------------------------------------//
  public String getCatalogText ()
  // ---------------------------------------------------------------------------------//
  {
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  // ---------------------------------------------------------------------------------//
  {
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getFile (int fileNo)
  // ---------------------------------------------------------------------------------//
  {
    return null;
  }

  // ---------------------------------------------------------------------------------//
  private void extractRewards (DefaultMutableTreeNode node, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> nodeSectors = new ArrayList<> ();
    ScenarioData sd = scenarioHeader.data.get (Header.TREASURE_TABLE_AREA);
    rewards = new ArrayList<> (sd.total);
    int max = sd.totalBlocks / 2;

    int seq = 0;
    for (int i = 0; i < max; i++)
    {
      List<DiskAddress> blocks = getTwoBlocks (sd, i, sectors);
      nodeSectors.addAll (blocks);
      byte[] buffer = disk.readBlocks (blocks);
      seq = addReward (buffer, blocks, node, seq);
    }

    StringBuilder text = new StringBuilder ();
    for (Reward t : rewards)
      text.append (t.getDump () + "\n");

    DefaultAppleFileSource afs = (DefaultAppleFileSource) node.getUserObject ();
    afs.setSectors (nodeSectors);
    DefaultDataSource dds = (DefaultDataSource) afs.getDataSource ();
    dds.text = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private int addReward (byte[] buffer, List<DiskAddress> blocks,
      DefaultMutableTreeNode node, int seq)
  // ---------------------------------------------------------------------------------//
  {
    int recLen = 168;
    for (int ptr = 0; ptr < 1008; ptr += recLen)
    {
      byte[] data2 = new byte[recLen];
      System.arraycopy (buffer, ptr, data2, 0, recLen);

      Reward tt = new Reward ("Type " + seq, data2, seq++, items);
      rewards.add (tt);
      addToNode (tt, node, blocks, treasureSector);
    }
    return seq;
  }

  // ---------------------------------------------------------------------------------//
  private void extractCharacters (DefaultMutableTreeNode node, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> nodeSectors = new ArrayList<> ();
    ScenarioData sd = scenarioHeader.data.get (Header.CHARACTER_AREA);
    characters = new ArrayList<> (sd.total);
    int max = sd.totalBlocks / 2;
    if (max < sd.total)
      System.out.println ("Characters short in Wizardry disk");

    for (int i = 0; i < max; i++)
    {
      List<DiskAddress> blocks = getTwoBlocks (sd, i, sectors);
      nodeSectors.addAll (blocks);
      byte[] buffer = disk.readBlocks (blocks);
      addCharacters (buffer, blocks, node);
    }

    StringBuilder text = new StringBuilder ();
    text.append ("Name            Age Align    Race     Type       "
        + "HP  St  In  Pi  Vi  Ag  Lu Status\n");
    text.append ("-------------  ---- -------- -------- ---------- "
        + "--  --  --  --  --  --  -- ------\n");
    for (Character ch : characters)
    {
      Statistics stats = ch.getStatistics ();
      Attributes att = ch.getAttributes ();
      text.append (
          String.format ("%-15s %2d  %-8s %-8s %-8s  %3d", ch, (stats.ageInWeeks / 52),
              stats.alignment, stats.race, stats.type, stats.hitsMax));
      text.append (String.format ("  %2d  %2d  %2d  %2d  %2d  %2d", att.strength,
          att.intelligence, att.piety, att.vitality, att.agility, att.luck));
      text.append (
          String.format ("  %5s  %s%n", stats.status, ch.isOut () ? "* OUT *" : ""));
    }

    DefaultAppleFileSource afs = (DefaultAppleFileSource) node.getUserObject ();
    afs.setSectors (nodeSectors);
    DefaultDataSource dds = (DefaultDataSource) afs.getDataSource ();
    dds.text = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private void addCharacters (byte[] buffer, List<DiskAddress> blocks,
      DefaultMutableTreeNode node)
  // ---------------------------------------------------------------------------------//
  {
    int recLen = 208;
    for (int ptr = 0; ptr < 832; ptr += recLen)
    {
      int nameLength = buffer[ptr] & 0xFF;
      if (nameLength == 0xC3 || buffer[ptr + 40] == 0x07)
        continue;
      String name = HexFormatter.getString (buffer, ptr + 1, nameLength);

      byte[] data2 = new byte[recLen];
      System.arraycopy (buffer, ptr, data2, 0, recLen);

      Character c = new Character (name, data2, scenarioHeader.scenarioID);
      characters.add (c);
      addToNode (c, node, blocks, characterSector);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void extractMonsters (DefaultMutableTreeNode node, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> nodeSectors = new ArrayList<> ();
    ScenarioData sd = scenarioHeader.data.get (Header.MONSTER_AREA);
    monsters = new ArrayList<> (sd.total);
    int max = sd.totalBlocks / 2;

    for (int i = 0; i < max; i++)
    {
      List<DiskAddress> blocks = getTwoBlocks (sd, i, sectors);
      nodeSectors.addAll (blocks);
      byte[] buffer = disk.readBlocks (blocks);
      addMonsters (buffer, blocks, node);
    }

    StringBuilder text = new StringBuilder ();
    for (int block = 0; block < 4; block++)
    {
      text.append (" ID    Name\n");
      text.append ("--- ---------------");
      for (int i = 0; i < 24; i++)
        text.append (" --");
      text.append ("\n");
      for (Monster m : monsters)
        text.append (m.getDump (block) + "\n");
      text.append ("\n");
    }
    DefaultAppleFileSource afs = (DefaultAppleFileSource) node.getUserObject ();
    afs.setSectors (nodeSectors);
    DefaultDataSource dds = (DefaultDataSource) afs.getDataSource ();
    dds.text = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private void addMonsters (byte[] buffer, List<DiskAddress> blocks,
      DefaultMutableTreeNode node)
  // ---------------------------------------------------------------------------------//
  {
    int recLen = 158;
    for (int ptr = 0; ptr < 948; ptr += recLen)
    {
      int nameLength = buffer[ptr + 32] & 0xFF;
      if (nameLength == 0 || nameLength == 255)
        break;
      String itemName = HexFormatter.getString (buffer, ptr + 33, nameLength);

      byte[] data2 = new byte[recLen];
      System.arraycopy (buffer, ptr, data2, 0, recLen);

      Monster m = new Monster (itemName, data2, rewards, monsters);
      monsters.add (m);
      addToNode (m, node, blocks, monsterSector);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void extractItems (DefaultMutableTreeNode node, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> nodeSectors = new ArrayList<> ();
    ScenarioData sd = scenarioHeader.data.get (Header.ITEM_AREA);
    items = new ArrayList<> (sd.total);
    int max = sd.totalBlocks / 2;

    for (int i = 0; i < max; i++)
    {
      List<DiskAddress> blocks = getTwoBlocks (sd, i, sectors);
      nodeSectors.addAll (blocks);
      byte[] buffer = disk.readBlocks (blocks);
      addItems (buffer, blocks, node);
    }

    StringBuilder text = new StringBuilder ();
    for (int block = 0; block < 3; block++)
    {
      text.append (" ID    Name\n");
      text.append ("--- ---------------");
      for (int i = 0; i < 24; i++)
        text.append (" --");
      text.append ("\n");
      for (Item item : items)
        text.append (item.getDump (block) + "\n");
      text.append ("\n");
    }
    DefaultAppleFileSource afs = (DefaultAppleFileSource) node.getUserObject ();
    afs.setSectors (nodeSectors);
    DefaultDataSource dds = (DefaultDataSource) afs.getDataSource ();
    dds.text = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private void addItems (byte[] buffer, List<DiskAddress> blocks,
      DefaultMutableTreeNode node)
  // ---------------------------------------------------------------------------------//
  {
    int recLen = 78;
    for (int ptr = 0; ptr < 1014; ptr += recLen)
    {
      if (buffer[ptr] == 0)
        break;
      String itemName = HexFormatter.getPascalString (buffer, ptr);

      byte[] data2 = new byte[recLen];
      System.arraycopy (buffer, ptr, data2, 0, recLen);

      Item i = new Item (itemName, data2);
      items.add (i);
      addToNode (i, node, blocks, itemSector);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void extractSpells (DefaultMutableTreeNode node, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    spells = new ArrayList<> ();
    List<DiskAddress> blocks = new ArrayList<> (2);
    int offset = scenarioHeader.scenarioID <= 2 ? 4 : 1;
    blocks.add (sectors.get (offset));
    blocks.add (sectors.get (offset + 1));

    SpellType spellType = SpellType.MAGE;
    for (DiskAddress da : blocks)
    {
      byte[] buffer = disk.readBlock (da);
      int level = 1;
      int ptr = -1;
      while (ptr < 255)
      {
        ptr++;
        int start = ptr;
        while (ptr < 256 && buffer[ptr] != 0x0D)
          ptr++;
        if (ptr == start)
          break;
        String spell = HexFormatter.getString (buffer, start, ptr - start);
        if (spell.startsWith ("*"))
        {
          spell = spell.substring (1);
          ++level;
        }
        Spell s = Spell.getSpell (spell, spellType, level, buffer);
        spells.add (s);
        //				addToNode (s, node, da, spellSector);
      }
      spellType = SpellType.PRIEST;
    }
  }

  // ---------------------------------------------------------------------------------//
  private void extractMessages (DefaultMutableTreeNode node, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    Message.resetMessageId ();
    messages = new ArrayList<> ();

    // Copy first 504 bytes from each sector to a single contiguous buffer
    int recordLength = 42;
    int max = recordLength * 12;
    byte[] buffer = new byte[sectors.size () * max];
    int offset = 0;

    for (DiskAddress da : sectors)
    {
      byte[] tempBuffer = disk.readBlock (da);
      System.arraycopy (tempBuffer, 0, buffer, offset, max);
      offset += max;
    }

    //    int id = 0;
    int totalLines = 0;

    for (int ptr = 0; ptr < buffer.length; ptr += recordLength)
    {
      int sequence = buffer[ptr + recordLength - 2];
      ++totalLines;
      if (sequence == 1) // end of message
      {
        int totalBytes = totalLines * recordLength;
        byte[] newBuffer = new byte[totalBytes];
        int messageEnd = ptr + recordLength;
        int messageStart = messageEnd - totalBytes;
        System.arraycopy (buffer, messageStart, newBuffer, 0, totalBytes);

        Message m;
        if (scenarioHeader.scenarioID == 1)
          m = new PlainMessage (newBuffer);
        else
          m = new CodedMessage (newBuffer);
        messages.add (m);

        List<DiskAddress> messageBlocks = new ArrayList<> ();
        int lastBlock = -1;
        for (int p2 = messageStart; p2 < messageEnd; p2 += recordLength)
        {
          int blockNo = p2 / max;
          offset = p2 % max;
          if (blockNo != lastBlock)
          {
            messageBlocks.add (sectors.get (blockNo));
            lastBlock = blockNo;
          }
        }
        addToNode (m, node, messageBlocks, messageSector);
        //        id += totalLines;
        totalLines = 0;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void extractLevels (DefaultMutableTreeNode node, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> nodeSectors = new ArrayList<> ();
    ScenarioData sd = scenarioHeader.data.get (Header.MAZE_AREA);
    levels = new ArrayList<> (sd.total);
    int max = sd.totalBlocks / 2;

    for (int i = 0; i < max; i++)
    {
      List<DiskAddress> blocks = getTwoBlocks (sd, i, sectors);
      nodeSectors.addAll (blocks);
      byte[] buffer = disk.readBlocks (blocks);
      byte[] data2 = new byte[896];
      System.arraycopy (buffer, 0, data2, 0, data2.length);
      //      System.out.println (HexFormatter.format (data2));

      MazeLevel mazeLevel = new MazeLevel (data2, i + 1);
      mazeLevel.setMessages (messages);
      mazeLevel.setMonsters (monsters);
      mazeLevel.setItems (items);
      levels.add (mazeLevel);
      addToNode (mazeLevel, node, blocks, mazeSector);
    }

    StringBuilder text = new StringBuilder ();
    for (MazeLevel level : levels)
      text.append (level.getName () + "\n");

    DefaultAppleFileSource afs = (DefaultAppleFileSource) node.getUserObject ();
    afs.setSectors (nodeSectors);
    DefaultDataSource dds = (DefaultDataSource) afs.getDataSource ();
    dds.text = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private void extractImages (DefaultMutableTreeNode node, List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> nodeSectors = new ArrayList<> ();
    ScenarioData sd = scenarioHeader.data.get (Header.IMAGE_AREA);
    int max = sd.totalBlocks;
    images = new ArrayList<> ();

    for (int i = 0; i < max; i++)
    {
      DiskAddress da = sectors.get (sd.dataOffset + i);
      nodeSectors.add (da);
      byte[] buffer = disk.readBlock (da);
      byte[] exactBuffer = new byte[480];
      System.arraycopy (buffer, 0, exactBuffer, 0, exactBuffer.length);

      String name = "Unknown";
      for (Monster m : monsters)
        if (m.imageID == i)
        {
          name = m.genericName;
          break;
        }

      AbstractImage mi = scenarioHeader.scenarioID < 3 ? new Image (name, buffer)
          : new ImageV2 (name, exactBuffer);
      images.add (mi);
      addToNode (mi, node, da, imageSector);
    }

    StringBuilder text = new StringBuilder ();
    for (AbstractImage image : images)
      text.append (image.getName () + "\n");

    DefaultAppleFileSource afs = (DefaultAppleFileSource) node.getUserObject ();
    afs.setSectors (nodeSectors);
    DefaultDataSource dds = (DefaultDataSource) afs.getDataSource ();
    dds.text = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private void extractExperienceLevels (DefaultMutableTreeNode node,
      List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> nodeSectors = new ArrayList<> ();
    ScenarioData sd = scenarioHeader.data.get (Header.EXPERIENCE_AREA);
    experiences = new ArrayList<> (sd.total);
    int max = sd.totalBlocks / 2;

    for (int i = 0; i < max; i++)
    {
      List<DiskAddress> blocks = getTwoBlocks (sd, i, sectors);
      nodeSectors.addAll (blocks);
      byte[] buffer = disk.readBlocks (blocks);

      for (int ptr = 0; ptr <= buffer.length; ptr += 78)
      {
        if (buffer[ptr] == 0)
          break;

        byte[] newBuffer = new byte[78];
        System.arraycopy (buffer, ptr, newBuffer, 0, newBuffer.length);
        ExperienceLevel el = new ExperienceLevel ("exp", newBuffer);
        experiences.add (el);
        addToNode (el, node, blocks, experienceSector);
      }
    }

    DefaultAppleFileSource afs = (DefaultAppleFileSource) node.getUserObject ();
    afs.setSectors (nodeSectors);
  }

  // ---------------------------------------------------------------------------------//
  private void addToNode (AbstractFile af, DefaultMutableTreeNode node, DiskAddress block,
      SectorType type)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> blocks = new ArrayList<> (1);
    blocks.add (block);
    addToNode (af, node, blocks, type);
  }

  // ---------------------------------------------------------------------------------//
  private void addToNode (AbstractFile af, DefaultMutableTreeNode node,
      List<DiskAddress> blocks, SectorType type)
  // ---------------------------------------------------------------------------------//
  {
    DefaultAppleFileSource dafs =
        new DefaultAppleFileSource (af.getName (), af, this, blocks);
    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode (dafs);
    node.add (childNode);
    childNode.setAllowsChildren (false);
  }

  // ---------------------------------------------------------------------------------//
  private List<DiskAddress> getTwoBlocks (ScenarioData sd, int i,
      List<DiskAddress> sectors)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> blocks = new ArrayList<> (2);
    blocks.add (sectors.get (sd.dataOffset + i * 2));
    blocks.add (sectors.get (sd.dataOffset + i * 2 + 1));
    return blocks;
  }
}