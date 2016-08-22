package com.bytezone.diskbrowser.wizardry;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class Monster extends AbstractFile implements Comparable<Monster>
{
  public final String genericName;
  public final String realName;
  public final int monsterID;
  List<Monster> monsters;
  Reward goldReward;
  Reward chestReward;

  public final int type;
  public final int imageID;
  int rewardTable1;
  int rewardTable2;
  public final int partnerID;
  public final int partnerOdds;
  public final int armourClass;
  public final int speed;
  public final int mageSpellLevel;
  public final int priestSpellLevel;
  int levelDrain;
  int bonus1;
  int bonus2;
  int bonus3;
  int resistance;
  int abilities;
  public final Dice groupSize, hitPoints;
  List<Dice> damage = new ArrayList<Dice> ();

  static int counter = 0;
  static boolean debug = true;
  static int[] pwr = { 0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 0, 0, 0, 0, 0 };
  static int[] weight1 = { 0, 1, 2, 4, 8, 16, 32, 64, 253, 506, 0 };
  static int[] weight2 = { 0, 60, 120, 180, 300, 540, 1020, 0 };

  public static String[] monsterClass =
      { "Fighter", "Mage", "Priest", "Thief", "Midget", "Giant", "Mythical", "Dragon",
        "Animal", "Were", "Undead", "Demon", "Insect", "Enchanted" };

  private static int[] experience =
      { 55, 235, 415, 230, 380, 620, 840, 520, 550, 350, // 00-09
        475, 515, 920, 600, 735, 520, 795, 780, 990, 795, // 10-19
        1360, 1320, 1275, 680, 960, 600, 755, 1120, 2075, 870, // 20-29
        960, 1120, 1120, 2435, 1080, 2280, 975, 875, 1135, 1200, // 30-39
        620, 740, 1460, 1245, 960, 1405, 1040, 1220, 1520, 1000, // 40-49
        960, 2340, 2160, 2395, 790, 1140, 1235, 1790, 1720, 2240, // 50-59
        1475, 1540, 1720, 1900, 1240, 1220, 1020, 20435, 5100, 3515, // 60-69
        2115, 2920, 2060, 2140, 1400, 1640, 1280, 4450, 42840, 3300, // 70-79
        40875, 5000, 3300, 2395, 1935, 1600, 3330, 44090, 40840, 5200, // 80-89
        4155, 3000, 9200, 3160, 7460, 7320, 15880, 1600, 2200, 1000, 1900 // 90-100
      };

  public Monster (String name, byte[] buffer, List<Reward> rewards,
      List<Monster> monsters)
  {
    super (name, buffer);

    realName = name;
    genericName = HexFormatter.getPascalString (buffer, 0);
    this.monsterID = counter++;
    this.monsters = monsters;
    goldReward = rewards.get (buffer[136]);
    chestReward = rewards.get (buffer[138]);
    goldReward.addMonster (this, 0);
    chestReward.addMonster (this, 1);

    imageID = buffer[64];
    type = buffer[78];
    armourClass = buffer[80];
    speed = buffer[82];
    levelDrain = buffer[132];
    bonus1 = buffer[134];
    rewardTable1 = buffer[136];
    rewardTable2 = buffer[138];
    partnerID = buffer[140];
    partnerOdds = buffer[142];
    mageSpellLevel = buffer[144];
    priestSpellLevel = buffer[146];
    bonus2 = buffer[150];
    bonus3 = buffer[152];
    resistance = buffer[154];
    abilities = buffer[156];
    groupSize = new Dice (buffer, 66);
    hitPoints = new Dice (buffer, 72);

    for (int i = 0, ptr = 84; i < 8; i++, ptr += 6)
    {
      if (buffer[ptr] == 0)
        break;
      damage.add (new Dice (buffer, ptr));
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    // these values definitely affect the damage a monster does (when breathing?)
    int exp2 =
        (HexFormatter.intValue (buffer[72]) * HexFormatter.intValue (buffer[74]) - 1)
            * 20;
    int exp3 = weight2[speed]; // 1-6
    int exp4 = (10 - armourClass) * 40;
    int exp5 = getBonus (35, mageSpellLevel);
    int exp6 = getBonus (35, priestSpellLevel);
    int exp10 = getBonus (200, levelDrain);
    int exp8 = getBonus (90, bonus1);
    int exp7 = weight1[bonus3 / 10] * 80;
    int exp11 = bonus2 > 0 ? exp2 + 20 : 0;
    int exp12 = getBonus (35, Integer.bitCount (resistance & 0x7E));
    int exp9 = getBonus (40, Integer.bitCount (abilities & 0x7F));

    text.append ("ID .............. " + monsterID);
    text.append ("\nMonster name .... " + realName);
    text.append ("\nGeneric name .... " + genericName);

    text.append ("\n\nImage ID ........ " + imageID);
    text.append ("\nGroup size ...... " + groupSize);
    text.append ("\nHit points ...... " + hitPoints);
    if (debug)
      text.append ("           " + exp2);

    text.append ("\n\nMonster class ... " + type + " " + monsterClass[type]);
    text.append ("\nArmour class .... " + armourClass);
    if (debug)
      text.append ("           " + exp4);
    text.append ("\nSpeed ........... " + speed);
    if (debug)
      text.append ("           " + exp3);

    text.append ("\n\nDamage .......... " + getDamage ());

    text.append ("\n\nLevel drain ..... " + levelDrain);
    if (debug)
      text.append ("           " + exp10);
    text.append ("\nExtra hit pts? .. " + bonus1);
    if (debug)
      text.append ("           " + exp8);

    text.append ("\n\nPartner ID ...... " + partnerID);
    if (partnerOdds > 0)
      text.append ("   " + monsters.get (partnerID).name);
    text.append ("\nPartner odds .... " + partnerOdds + "%");

    text.append ("\n\nMage level ...... " + mageSpellLevel);
    if (debug)
      text.append ("           " + exp5);
    text.append ("\nPriest level .... " + priestSpellLevel);
    if (debug)
      text.append ("           " + exp6);

    text.append ("\n\nExperience bonus  " + bonus2);
    if (debug)
      text.append ("           " + exp11);
    text.append ("\nExperience bonus  " + bonus3);
    if (debug)
      text.append ("           " + exp7);

    text.append ("\n\nResistance ...... " + String.format ("%02X", resistance));
    if (debug)
      text.append ("           " + exp12);
    text.append ("\nAbilities ....... " + String.format ("%02X", abilities));
    if (debug)
      text.append ("           " + exp9);

    text.append ("\n\nExperience ...... " + (exp2 + exp3 + exp4 + exp5 + exp6 + exp7
        + exp8 + exp9 + exp10 + exp11 + exp12));

    text.append ("\n\n===== Gold reward ======");
    //		text.append ("\nTable ........... " + rewardTable1);
    text.append ("\n" + goldReward.getText (false));
    text.append ("===== Chest reward =====");
    //		text.append ("\nTable ........... " + rewardTable2);
    text.append ("\n" + chestReward.getText (false));

    while (text.charAt (text.length () - 1) == 10)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  public int getExperience ()
  {
    // these values definitely affect the damage a monster does (when breathing?)
    int exp2 =
        (HexFormatter.intValue (buffer[72]) * HexFormatter.intValue (buffer[74]) - 1)
            * 20;
    int exp3 = weight2[speed];
    int exp4 = (10 - armourClass) * 40;
    int exp5 = getBonus (35, mageSpellLevel);
    int exp6 = getBonus (35, priestSpellLevel);
    int exp10 = getBonus (200, levelDrain);
    int exp8 = getBonus (90, bonus1);
    int exp7 = weight1[bonus3 / 10] * 80;
    int exp11 = bonus2 > 0 ? exp2 + 20 : 0;
    int exp12 = getBonus (35, Integer.bitCount (resistance & 0x7E));
    int exp9 = getBonus (40, Integer.bitCount (abilities & 0x7F));
    return exp2 + exp3 + exp4 + exp5 + exp6 + exp7 + exp8 + exp9 + exp10 + exp11 + exp12;
  }

  private int getBonus (int base, int value)
  {
    return base * pwr[value];
  }

  public void setImage (BufferedImage image)
  {
    this.image = image;
  }

  @Override
  public String getName ()
  {
    return realName;
  }

  public String getRealName ()
  {
    return realName;
  }

  public String getDamage ()
  {
    StringBuilder text = new StringBuilder ();
    for (Dice d : damage)
      text.append (d + ", ");
    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  public String getDump (int block)
  {
    StringBuilder line =
        new StringBuilder (String.format ("%3d %-16s", monsterID, realName));
    int lo = block == 0 ? 64 : block == 1 ? 88 : block == 2 ? 112 : 136;
    int hi = lo + 24;
    if (hi > buffer.length)
      hi = buffer.length;
    for (int i = lo; i < hi; i++)
      line.append (String.format ("%02X ", buffer[i]));
    if (block == 3)
    {
      int exp = getExperience ();
      line.append (String.format (" %,6d", exp));
      if (exp != experience[monsterID])
        line.append (String.format ("  %,6d", experience[monsterID]));
    }
    return line.toString ();
  }

  public boolean match (int monsterID)
  {
    return this.monsterID == monsterID;
  }

  @Override
  public int compareTo (Monster other)               // where is this used?
  {
    if (this.type == other.type)
      return 0;
    if (this.type < other.type)
      return -1;
    return 1;
  }

  @Override
  public String toString ()
  {
    return realName;
  }
}