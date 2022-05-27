package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class Header
// -----------------------------------------------------------------------------------//
{
  static String[] typeText =
      { "header", "maze", "monsters", "rewards", "items", "characters", "images", "char levels" };
  static String[] scenarioNames = { "PROVING GROUNDS OF THE MAD OVERLORD!",
      "THE KNIGHT OF DIAMONDS", "THE LEGACY OF LLYLGAMYN", "THE RETURN OF WERDNA" };

  static final int MAZE_AREA = 1;
  static final int MONSTER_AREA = 2;
  static final int TREASURE_TABLE_AREA = 3;
  static final int ITEM_AREA = 4;
  static final int CHARACTER_AREA = 5;
  static final int IMAGE_AREA = 6;
  static final int EXPERIENCE_AREA = 7;

  String scenarioTitle;
  public int scenarioID;
  List<ScenarioData> data = new ArrayList<> (8);
  FormattedDisk owner;

  // ---------------------------------------------------------------------------------//
  Header (DefaultMutableTreeNode dataNode, FormattedDisk owner)
  // ---------------------------------------------------------------------------------//
  {
    this.owner = owner;

    AppleFileSource afs = (AppleFileSource) dataNode.getUserObject ();
    List<DiskAddress> sectors = afs.getSectors ();
    DefaultAppleFile daf = (DefaultAppleFile) afs.getDataSource ();
    scenarioTitle = HexFormatter.getPascalString (daf.buffer, 0);

    while (scenarioID < scenarioNames.length)
      if (scenarioNames[scenarioID++].equals (scenarioTitle))
        break;

    if (scenarioID > scenarioNames.length)
      System.out.println ("Invalid scenario ID : " + scenarioID + " " + scenarioTitle);

    for (int i = 0; i < 8; i++)
      data.add (new ScenarioData (daf.buffer, i, sectors));

    StringBuilder text = new StringBuilder ("Data type     Offset   Size  Units    ???\n"
        + "------------  ------  -----  -----  -----\n");

    for (ScenarioData sd : data)
      text.append (sd + "\n");

    daf.setText (text.toString ());

    text = new StringBuilder (scenarioTitle + "\n\n");

    int ptr = 106;
    while (daf.buffer[ptr] != -1)
    {
      text.append (HexFormatter.getPascalString (daf.buffer, ptr) + "\n");
      ptr += 10;
    }

    DefaultAppleFileSource dafs = new DefaultAppleFileSource ("Header", text.toString (), owner);
    dafs.setSectors (data.get (0).sectors);
    DefaultMutableTreeNode headerNode = new DefaultMutableTreeNode (dafs);
    dataNode.add (headerNode);

    if (scenarioID > 3)
      return;

    int totalBlocks = data.get (0).sectors.size ();
    linkText ("Text", data.get (0).sectors.get (0), headerNode);

    if (scenarioID < 3)
    {
      linkPictures ("Alphabet", data.get (0).sectors.get (1), headerNode);
      linkPictures ("Graphics", data.get (0).sectors.get (2), headerNode);
      linkPictures ("Unknown", data.get (0).sectors.get (3), headerNode);
    }

    linkSpells ("Mage spells", data.get (0).sectors.get (totalBlocks - 2), headerNode);
    linkSpells ("Priest spells", data.get (0).sectors.get (totalBlocks - 1), headerNode);

    if (false && scenarioID <= 2)
    {
      System.out.println (printChars (daf.buffer, 1));
      System.out.println (printChars (daf.buffer, 2));
    }
  }

  // ---------------------------------------------------------------------------------//
  ScenarioData get (int index)
  // ---------------------------------------------------------------------------------//
  {
    return data.get (index);
  }

  // ---------------------------------------------------------------------------------//
  private void linkText (String title, DiskAddress da, DefaultMutableTreeNode headerNode)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> blocks = new ArrayList<> ();
    blocks.add (da);

    StringBuilder text = new StringBuilder (scenarioTitle + "\n\n");

    int ptr = 106;
    byte[] buffer = owner.getDisk ().readBlock (da);
    while (buffer[ptr] != -1)
    {
      text.append (HexFormatter.getPascalString (buffer, ptr) + "\n");
      ptr += 10;
    }
    ptr += 2;
    text.append ("\n");
    while (ptr < 512)
    {
      int value = Utility.getShort (buffer, ptr);
      text.append (String.format ("%04X  %,6d%n", value, value));
      ptr += 2;
    }

    DefaultAppleFileSource dafs = new DefaultAppleFileSource (title, text.toString (), owner);
    dafs.setSectors (blocks);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode (dafs);
    node.setAllowsChildren (false);
    headerNode.add (node);
  }

  // ---------------------------------------------------------------------------------//
  private void linkPictures (String title, DiskAddress da, DefaultMutableTreeNode headerNode)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> blocks = new ArrayList<> ();
    blocks.add (da);

    byte[] buffer = owner.getDisk ().readBlock (da);
    String text = printChars (buffer, 0);

    DefaultAppleFileSource dafs = new DefaultAppleFileSource (title, text, owner);
    dafs.setSectors (blocks);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode (dafs);
    node.setAllowsChildren (false);
    headerNode.add (node);
  }

  // ---------------------------------------------------------------------------------//
  private void linkSpells (String title, DiskAddress da, DefaultMutableTreeNode headerNode)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> blocks = new ArrayList<> ();
    blocks.add (da);
    int level = 1;

    StringBuilder list = new StringBuilder ("Level " + level + ":\n");
    byte[] buffer = owner.getDisk ().readBlock (da);
    String text = HexFormatter.getString (buffer, 0, 512);
    String[] spells = text.split ("\n");
    for (String s : spells)
    {
      if (s.length () == 0)
        break;
      if (s.startsWith ("*"))
      {
        s = s.substring (1);
        level++;
        list.append ("\nLevel " + level + ":\n");
      }
      list.append ("  " + s + "\n");
    }

    DefaultAppleFileSource dafs = new DefaultAppleFileSource (title, list.toString (), owner);
    dafs.setSectors (blocks);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode (dafs);
    node.setAllowsChildren (false);
    headerNode.add (node);
  }

  // ---------------------------------------------------------------------------------//
  private String printChars (byte[] buffer, int block)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    for (int i = block * 512; i < (block + 1) * 512; i += 64)
    {
      for (int line = 0; line < 8; line++)
      {
        for (int j = 0; j < 8; j++)
        {
          int value = buffer[i + line + j * 8] & 0xFF;
          for (int bit = 0; bit < 7; bit++)
          {
            if ((value & 0x01) == 1)
              text.append ("O");
            else
              text.append (".");
            value >>= 1;
          }
          text.append ("   ");
        }
        text.append ("\n");
      }
      text.append ("\n");
    }
    return text.toString ();
  }

  // this could be the base factory class for all Wizardry types
  // ---------------------------------------------------------------------------------//
  class ScenarioData
  // ---------------------------------------------------------------------------------//
  {
    int dunno;
    int total;
    int totalBlocks;                // size in blocks
    int dataOffset;                 // first block
    int type;
    List<DiskAddress> sectors;

    public ScenarioData (byte[] buffer, int seq, List<DiskAddress> sectors)
    {
      int offset = 42 + seq * 2;
      dunno = buffer[offset] & 0xFF;
      total = buffer[offset + 16] & 0xFF;
      totalBlocks = buffer[offset + 32] & 0xFF;
      dataOffset = buffer[offset + 48] & 0xFF;
      type = seq;

      this.sectors = new ArrayList<> (totalBlocks);
      for (int i = dataOffset, max = dataOffset + totalBlocks; i < max; i++)
        if (i < sectors.size ())
          this.sectors.add (sectors.get (i));
    }

    @Override
    public String toString ()
    {
      return String.format ("%-15s  %3d    %3d    %3d    %3d", typeText[type], dataOffset,
          totalBlocks, total, dunno);
    }
  }
}