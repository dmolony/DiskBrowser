package com.bytezone.diskbrowser.wizardry;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class Monster extends AbstractFile
// -----------------------------------------------------------------------------------//
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
  public final int recsn;
  public final int mageSpellLevel;
  public final int priestSpellLevel;

  int levelDrain;
  int healPts;
  int breathe;
  int unaffect;
  int unique;
  int resistance;
  int abilities;
  public final Dice groupSize, hitPoints;
  List<Dice> damage = new ArrayList<> ();

  static int counter = 0;

  public static String[] monsterClass = { "Fighter", "Mage", "Priest", "Thief", "Midget", "Giant",
      "Mythical", "Dragon", "Animal", "Were", "Undead", "Demon", "Insect", "Enchanted" };

  private static int[] experience = {                                     //
      55, 235, 415, 230, 380, 620, 840, 520, 550, 350,                    // 00-09
      475, 515, 920, 600, 735, 520, 795, 780, 990, 795,                   // 10-19
      1360, 1320, 1275, 680, 960, 600, 755, 1120, 2075, 870,              // 20-29
      960, 600, 1120, 2435, 1080, 2280, 975, 875, 1135, 1200,             // 30-39
      620, 740, 1460, 1245, 960, 1405, 1040, 1220, 1520, 1000,            // 40-49
      960, 2340, 2160, 2395, 790, 1140, 1235, 1790, 1720, 2240,           // 50-59
      1475, 1540, 1720, 1900, 1240, 1220, 1020, 20435, 5100, 3515,        // 60-69
      2115, 2920, 2060, 2140, 1400, 1640, 1280, 4450, 42840, 3300,        // 70-79
      40875, 5000, 3300, 2395, 1935, 1600, 3330, 44090, 40840, 5200,      // 80-89
      4155, 3000, 9200, 3160, 7460, 7320, 15880, 1600, 2200, 1000,        // 90-99
      1900                                                                // 100 
  };

  // ---------------------------------------------------------------------------------//
  Monster (String name, byte[] buffer, List<Reward> rewards, List<Monster> monsters)
  // ---------------------------------------------------------------------------------//
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
    groupSize = new Dice (buffer, 66);
    hitPoints = new Dice (buffer, 72);
    type = buffer[78];
    armourClass = buffer[80];

    recsn = buffer[82];                               // number of dice
    for (int i = 0, ptr = 84; i < 8; i++, ptr += 6)
    {
      if (buffer[ptr] == 0)
        break;
      damage.add (new Dice (buffer, ptr));
    }

    levelDrain = buffer[132];
    healPts = buffer[134];
    rewardTable1 = buffer[136];
    rewardTable2 = buffer[138];
    partnerID = buffer[140];
    partnerOdds = buffer[142];
    mageSpellLevel = buffer[144];
    priestSpellLevel = buffer[146];

    unique = buffer[148];
    breathe = buffer[150];
    unaffect = buffer[152];

    resistance = buffer[154];     // bit flags
    abilities = buffer[156];      // bit flags
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int totalExperience = getExperience ();

    text.append ("ID .............. " + monsterID);
    text.append ("\nMonster name .... " + realName);
    text.append ("\nGeneric name .... " + genericName);

    text.append ("\n\nImage ID ........ " + imageID);
    text.append ("\nGroup size ...... " + groupSize);
    text.append ("\nHit points ...... " + hitPoints);

    text.append ("\n\nMonster class ... " + type + "   " + monsterClass[type]);
    text.append ("\nArmour class .... " + armourClass);

    text.append ("\n\n# damage ........ " + recsn);

    text.append ("\nDamage .......... " + getDamage ());

    text.append ("\n\nLevel drain ..... " + levelDrain);
    text.append ("\nHeal pts? ....... " + healPts);

    text.append ("\n\nPartner ID ...... " + partnerID);
    if (partnerOdds > 0)
      text.append ("   " + monsters.get (partnerID).getName ());
    text.append ("\nPartner odds .... " + partnerOdds + "%");

    text.append ("\n\nMage level ...... " + mageSpellLevel);
    text.append ("\nPriest level .... " + priestSpellLevel);

    text.append ("\n\nUnique .......... " + unique);
    text.append ("\nBreathe ......... " + breathe);
    text.append ("\nUnaffect ........ " + unaffect);

    text.append ("\n\nResistance ...... " + String.format ("%3d  %<02X", resistance));
    text.append ("\nAbilities ....... " + String.format ("%3d  %<02X", abilities));

    text.append (String.format ("%n%nExperience ...... %-,7d", totalExperience));

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

  // ---------------------------------------------------------------------------------//
  private int getExperience ()
  // ---------------------------------------------------------------------------------//
  {
    int expHitPoints = hitPoints.qty * hitPoints.sides * (breathe == 0 ? 20 : 40);
    int expAc = 40 * (11 - armourClass);

    int expMage = getBonus (35, mageSpellLevel);
    int expPriest = getBonus (35, priestSpellLevel);
    int expDrain = getBonus (200, levelDrain);
    int expHeal = getBonus (90, healPts);

    int expDamage = recsn <= 1 ? 0 : getBonus (30, recsn);
    int expUnaffect = unaffect == 0 ? 0 : getBonus (40, (unaffect / 10 + 1));

    int expFlags1 = getBonus (35, Integer.bitCount (resistance & 0x7E));
    int expFlags2 = getBonus (40, Integer.bitCount (abilities & 0x7F));

    return expHitPoints + expAc + expMage + expPriest + expDrain + expHeal + expDamage + expUnaffect
        + expFlags1 + expFlags2;
  }

  // ---------------------------------------------------------------------------------//
  private int getBonus (int base, int multiplier)
  // ---------------------------------------------------------------------------------//
  {
    if (multiplier == 0)
      return 0;

    int total = base;
    while (multiplier > 1)
    {
      multiplier--;
      total += total;        // double the value
    }

    return total + total / 10000 * 10000;       // mimics the Wizardry bug
  }

  // ---------------------------------------------------------------------------------//
  public void setImage (BufferedImage image)
  // ---------------------------------------------------------------------------------//
  {
    this.image = image;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    return realName;
  }

  // ---------------------------------------------------------------------------------//
  public String getRealName ()
  // ---------------------------------------------------------------------------------//
  {
    return realName;
  }

  // ---------------------------------------------------------------------------------//
  public String getDamage ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (Dice d : damage)
      text.append (d + ", ");

    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public String getDump (int block)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = new StringBuilder (String.format ("%3d %-16s", monsterID, realName));

    int lo = block == 0 ? 64 : block == 1 ? 88 : block == 2 ? 112 : 136;
    int hi = lo + 24;
    if (hi > buffer.length)
      hi = buffer.length;

    for (int i = lo; i < hi; i++)
      line.append (String.format ("%02X ", buffer[i]));

    if (block == 3)
    {
      int exp = getExperience ();
      line.append (String.format (" %,6d  %,6d", exp, exp - experience[monsterID]));
    }

    return line.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public boolean match (int monsterID)
  // ---------------------------------------------------------------------------------//
  {
    return this.monsterID == monsterID;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return realName;
  }
}