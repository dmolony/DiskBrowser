package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class Character extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private final Attributes attributes;
  private final Statistics stats;
  int scenario;

  private final Collection<Spell> spellBook = new ArrayList<> ();
  private final Collection<Baggage> baggageList = new ArrayList<> ();

  static String[] races = { "No race", "Human", "Elf", "Dwarf", "Gnome", "Hobbit" };
  static String[] alignments = { "Unalign", "Good", "Neutral", "Evil" };
  static String[] types =
      { "Fighter", "Mage", "Priest", "Thief", "Bishop", "Samurai", "Lord", "Ninja" };
  static String[] statuses =
      { "OK", "Afraid", "Asleep", "Paralyze", "Stoned", "Dead", "Ashes", "Lost" };

  // ---------------------------------------------------------------------------------//
  Character (String name, byte[] buffer, int scenario)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
    this.scenario = scenario;

    attributes = new Attributes ();
    stats = new Statistics ();

    stats.race = races[buffer[34] & 0xFF];
    stats.typeInt = buffer[36] & 0xFF;
    stats.type = types[stats.typeInt];
    stats.ageInWeeks = Utility.getShort (buffer, 38);
    stats.statusValue = buffer[40];
    stats.status = statuses[stats.statusValue];
    stats.alignment = alignments[buffer[42] & 0xFF];

    stats.gold = Utility.getShort (buffer, 52) + Utility.getShort (buffer, 54) * 10000;
    stats.experience =
        Utility.getShort (buffer, 124) + Utility.getShort (buffer, 126) * 10000;
    stats.level = Utility.getShort (buffer, 132);

    stats.hitsLeft = Utility.getShort (buffer, 134);
    stats.hitsMax = Utility.getShort (buffer, 136);
    stats.armourClass = buffer[176];

    attributes.strength = (buffer[44] & 0xFF) % 16;
    if (attributes.strength < 3)
      attributes.strength += 16;
    attributes.array[0] = attributes.strength;

    int i1 = (buffer[44] & 0xFF) / 16;
    int i2 = (buffer[45] & 0xFF) % 4;
    attributes.intelligence = i1 / 2 + i2 * 8;
    attributes.array[1] = attributes.intelligence;

    attributes.piety = (buffer[45] & 0xFF) / 4;
    attributes.array[2] = attributes.piety;

    attributes.vitality = (buffer[46] & 0xFF) % 16;
    if (attributes.vitality < 3)
      attributes.vitality += 16;
    attributes.array[3] = attributes.vitality;

    int a1 = (buffer[46] & 0xFF) / 16;
    int a2 = (buffer[47] & 0xFF) % 4;
    attributes.agility = a1 / 2 + a2 * 8;
    attributes.array[4] = attributes.agility;

    attributes.luck = (buffer[47] & 0xFF) / 4;
    attributes.array[5] = attributes.luck;
  }

  // ---------------------------------------------------------------------------------//
  public void linkItems (List<Item> itemList)
  // ---------------------------------------------------------------------------------//
  {
    boolean equipped;
    boolean identified;
    int totItems = buffer[58];
    stats.assetValue = 0;

    for (int ptr = 60; totItems > 0; ptr += 8, totItems--)
    {
      int itemID = buffer[ptr + 6] & 0xFF;
      if (scenario == 3)
        itemID = (itemID + 24) % 256;
      if (itemID >= 0 && itemID < itemList.size ())
      {
        Item item = itemList.get (itemID);
        equipped = (buffer[ptr] == 1);
        identified = (buffer[ptr + 4] == 1);
        baggageList.add (new Baggage (item, equipped, identified));
        stats.assetValue += item.getCost ();
        item.partyOwns++;
      }
      else
        System.out.println (getName () + " ItemID : " + itemID + " is outside range 0:"
            + (itemList.size () - 1));
    }
  }

  // ---------------------------------------------------------------------------------//
  public void linkSpells (List<Spell> spellList)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 138; i < 145; i++)
      for (int bit = 0; bit < 8; bit++)
        if (((buffer[i] >>> bit) & 1) == 1)
        {
          int index = (i - 138) * 8 + bit;
          if (index > 0 && index <= spellList.size ())
            spellBook.add (spellList.get (index - 1));
          else
            System.out.println ("LinkSpell: " + getName () + " SpellID : " + index
                + " is outside range 1:" + spellList.size ());
        }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Character name ..... " + getName ());
    text.append ("\n\nRace ............... " + stats.race);
    text.append ("\nType ............... " + stats.type);
    text.append ("\nAlignment .......... " + stats.alignment);
    text.append ("\nStatus ............. " + stats.status);
    //    text.append ("\nType ............... " + stats.typeInt);
    //    text.append ("\nStatus ............. " + stats.statusValue);
    text.append ("\nGold ............... " + String.format ("%,d", stats.gold));
    text.append ("\nExperience ......... " + String.format ("%,d", stats.experience));
    text.append ("\nNext level ......... " + String.format ("%,d", stats.nextLevel));
    text.append ("\nLevel .............. " + stats.level);
    text.append ("\nAge in weeks ....... "
        + String.format ("%,d  (%d)", stats.ageInWeeks, (stats.ageInWeeks / 52)));
    text.append ("\nHit points left .... " + stats.hitsLeft);
    text.append ("\nMaximum hits ....... " + stats.hitsMax);
    text.append ("\nArmour class ....... " + stats.armourClass);
    text.append ("\nAsset value ........ " + String.format ("%,d", stats.assetValue));
    text.append ("\nAwards ............. " + isWinner ());
    text.append ("\nOut ................ " + isOut ());
    text.append ("\n\nStrength ........... " + attributes.strength);
    text.append ("\nIntelligence ....... " + attributes.intelligence);
    text.append ("\nPiety .............. " + attributes.piety);
    text.append ("\nVitality ........... " + attributes.vitality);
    text.append ("\nAgility ............ " + attributes.agility);
    text.append ("\nLuck ............... " + attributes.luck);

    int[] spellPoints = getMageSpellPoints ();
    text.append ("\n\nMage spell points ..");
    for (int i = 0; i < spellPoints.length; i++)
      text.append (" " + spellPoints[i]);

    spellPoints = getPriestSpellPoints ();
    text.append ("\nPriest spell points ");
    for (int i = 0; i < spellPoints.length; i++)
      text.append (" " + spellPoints[i]);

    text.append ("\n\nSpells :");
    for (Spell s : spellBook)
      text.append ("\n" + s);

    text.append ("\n\nItems :");
    for (Baggage b : baggageList)
      text.append ("\n" + b);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public void linkExperience (ExperienceLevel exp)
  // ---------------------------------------------------------------------------------//
  {
    stats.nextLevel = exp.getExperiencePoints (stats.level);
  }

  // ---------------------------------------------------------------------------------//
  public int[] getMageSpellPoints ()
  // ---------------------------------------------------------------------------------//
  {
    int[] spells = new int[7];

    for (int i = 0; i < 7; i++)
      spells[i] = buffer[146 + i * 2];

    return spells;
  }

  // ---------------------------------------------------------------------------------//
  public int[] getPriestSpellPoints ()
  // ---------------------------------------------------------------------------------//
  {
    int[] spells = new int[7];

    for (int i = 0; i < 7; i++)
      spells[i] = buffer[160 + i * 2];

    return spells;
  }

  // ---------------------------------------------------------------------------------//
  public Long getNextLevel ()
  // ---------------------------------------------------------------------------------//
  {
    return stats.nextLevel;
  }

  // this is temporary until I have more data
  // ---------------------------------------------------------------------------------//
  public String isWinner ()
  // ---------------------------------------------------------------------------------//
  {
    int v1 = buffer[206];
    int v2 = buffer[207];
    if (v1 == 0x01)
      return ">";
    if (v1 == 0x00 && v2 == 0x00)
      return "";
    if (v1 == 0x00 && v2 == 0x20)
      return "D";
    if (v1 == 0x20 && v2 == 0x20)
      return "*D";
    if (v1 == 0x21 && v2 == 0x60)
      return ">*DG";
    if (v1 == 0x21 && v2 == 0x28)
      return ">*KD";
    return "Unknown : " + v1 + " " + v2;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isOut ()
  // ---------------------------------------------------------------------------------//
  {
    return (buffer[32] == 1);
  }

  // ---------------------------------------------------------------------------------//
  public String getType ()
  // ---------------------------------------------------------------------------------//
  {
    return stats.type;
  }

  // ---------------------------------------------------------------------------------//
  public String getRace ()
  // ---------------------------------------------------------------------------------//
  {
    return stats.race;
  }

  // ---------------------------------------------------------------------------------//
  public String getAlignment ()
  // ---------------------------------------------------------------------------------//
  {
    return stats.alignment;
  }

  // ---------------------------------------------------------------------------------//
  public Attributes getAttributes ()
  // ---------------------------------------------------------------------------------//
  {
    return attributes;
  }

  // ---------------------------------------------------------------------------------//
  public Statistics getStatistics ()
  // ---------------------------------------------------------------------------------//
  {
    return stats;
  }

  // ---------------------------------------------------------------------------------//
  public Iterator<Baggage> getBaggage ()
  // ---------------------------------------------------------------------------------//
  {
    return baggageList.iterator ();
  }

  // ---------------------------------------------------------------------------------//
  public Iterator<Spell> getSpells ()
  // ---------------------------------------------------------------------------------//
  {
    return spellBook.iterator ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return getName ();
  }

  // ---------------------------------------------------------------------------------//
  public class Baggage
  // ---------------------------------------------------------------------------------//
  {
    public Item item;
    public boolean equipped;
    public boolean identified;

    public Baggage (Item item, boolean equipped, boolean identified)
    {
      this.item = item;
      this.equipped = equipped;
      this.identified = identified;
    }

    @Override
    public String toString ()
    {
      return String.format ("%s%-15s (%d)", equipped ? "*" : " ", item.getName (),
          item.getCost ());
    }
  }

  // ---------------------------------------------------------------------------------//
  public class Statistics implements Cloneable
  // ---------------------------------------------------------------------------------//
  {
    public String race;
    public String type;
    public String alignment;
    public String status;
    public int typeInt;
    public int statusValue;
    public int gold;
    public int experience;
    public long nextLevel;
    public int level;
    public int ageInWeeks;
    public int hitsLeft;
    public int hitsMax;
    public int armourClass;
    public int assetValue;
  }

  public class Attributes
  {
    public int strength;
    public int intelligence;
    public int piety;
    public int vitality;
    public int agility;
    public int luck;
    public int[] array;

    public Attributes ()
    {
      array = new int[6];
    }
  }
}