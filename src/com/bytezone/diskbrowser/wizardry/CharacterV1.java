package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class CharacterV1 extends Character
// -----------------------------------------------------------------------------------//
{
  public final int[] attributes = new int[6];      // 0:18
  public final int[] saveVs = new int[5];          // 0:31

  private final Statistics stats;

  private final List<Spell> spellBook = new ArrayList<> ();
  private final List<Baggage> baggageList = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  CharacterV1 (String name, byte[] buffer, int scenario)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.scenario = scenario;

    stats = new Statistics ();

    stats.race = races[buffer[34] & 0xFF];
    stats.typeInt = buffer[36] & 0xFF;
    stats.type = types[stats.typeInt];
    stats.ageInWeeks = Utility.getShort (buffer, 38);
    stats.statusValue = buffer[40];
    stats.status = statuses[stats.statusValue];
    stats.alignment = alignments[buffer[42] & 0xFF];

    int attr1 = Utility.getShort (buffer, 44);
    int attr2 = Utility.getShort (buffer, 46);

    attributes[0] = attr1 & 0x001F;
    attributes[1] = (attr1 & 0x03E0) >>> 5;
    attributes[2] = (attr1 & 0x7C00) >>> 10;
    attributes[3] = attr2 & 0x001F;
    attributes[4] = (attr2 & 0x03E0) >>> 5;
    attributes[5] = (attr2 & 0x7C00) >>> 10;

    stats.gold = Utility.getWizLong (buffer, 52);
    stats.experience = Utility.getWizLong (buffer, 124);
    stats.level = Utility.getShort (buffer, 132);

    stats.hitsLeft = Utility.getShort (buffer, 134);
    stats.hitsMax = Utility.getShort (buffer, 136);

    stats.armourClass = buffer[176];

    // saving throws
    attr1 = Utility.getShort (buffer, 48);
    attr2 = Utility.getShort (buffer, 50);

    saveVs[0] = attr1 & 0x001F;
    saveVs[1] = (attr1 & 0x03E0) >>> 5;
    saveVs[2] = (attr1 & 0x7C00) >>> 10;
    saveVs[3] = attr2 & 0x001F;
    saveVs[4] = (attr2 & 0x03E0) >>> 5;
  }

  // ---------------------------------------------------------------------------------//
  public void linkItems (List<ItemV1> itemList)
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
        ItemV1 item = itemList.get (itemID);
        equipped = (buffer[ptr] == 1);
        identified = (buffer[ptr + 4] == 1);
        baggageList.add (new Baggage (item, equipped, identified));
        stats.assetValue += item.getCost ();
      }
      else
        System.out.println (
            getName () + " ItemID : " + itemID + " is outside range 0:" + (itemList.size () - 1));
    }
  }

  // ---------------------------------------------------------------------------------//
  public void linkSpells (List<Spell> spellList)
  // ---------------------------------------------------------------------------------//
  {
    int index = 0;
    for (int i = 138; i < 145; i++)
      for (int bit = 0; bit < 8; bit++)
      {
        if (((buffer[i] >>> bit) & 0x01) != 0)
          spellBook.add (spellList.get (index));

        if (++index >= spellList.size ())
          break;
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

    text.append ("\nAwards ............. " + getAwardString ());
    text.append ("\nOut ................ " + isOut ());

    text.append ("\n\nStrength ........... " + attributes[0]);
    text.append ("\nIntelligence ....... " + attributes[1]);
    text.append ("\nPiety .............. " + attributes[2]);
    text.append ("\nVitality ........... " + attributes[3]);
    text.append ("\nAgility ............ " + attributes[4]);
    text.append ("\nLuck ............... " + attributes[5]);

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
  int[] getAttributes ()
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
    public ItemV1 item;
    public boolean equipped;
    public boolean identified;

    public Baggage (ItemV1 item, boolean equipped, boolean identified)
    {
      this.item = item;
      this.equipped = equipped;
      this.identified = identified;
    }

    @Override
    public String toString ()
    {
      return String.format ("%s%-15s %,10d", equipped ? "*" : " ", item.getName (),
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
    public long gold;
    public int experience;
    public long nextLevel;
    public int level;
    public int ageInWeeks;
    public int hitsLeft;
    public int hitsMax;
    public int armourClass;
    public int assetValue;
  }
}