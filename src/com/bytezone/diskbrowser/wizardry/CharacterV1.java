package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class CharacterV1 extends Character
// -----------------------------------------------------------------------------------//
{
  private String race;
  private String type;
  private String alignment;
  private String status;

  public int typeInt;
  public int statusInt;

  public long gold;
  public int experience;
  public long nextLevel;
  public int ageInWeeks;
  public int assetValue;

  private final List<Spell> spellBook = new ArrayList<> ();
  private final List<Possession> possessions = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  CharacterV1 (String name, byte[] buffer, int scenario)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.scenario = scenario;

    inMaze = Utility.getShort (buffer, 32) != 0;
    race = races[buffer[34] & 0xFF];

    typeInt = buffer[36] & 0xFF;
    type = types[typeInt];

    ageInWeeks = Utility.getShort (buffer, 38);

    statusInt = buffer[40];
    status = statuses[statusInt];

    alignment = alignments[buffer[42] & 0xFF];

    get3x5Bits (attributes, 0, Utility.getShort (buffer, 44));
    get3x5Bits (attributes, 3, Utility.getShort (buffer, 46));
    get3x5Bits (saveVs, 0, Utility.getShort (buffer, 48));
    get2x5Bits (saveVs, 3, Utility.getShort (buffer, 50));

    gold = Utility.getWizLong (buffer, 52);

    possessionsCount = Utility.getShort (buffer, 58);
    for (int i = 0; i < possessionsCount; i++)
    {
      boolean equipped = Utility.getShort (buffer, 60 + i * 8) == 1;
      boolean cursed = Utility.getShort (buffer, 62 + i * 8) == 1;
      boolean identified = Utility.getShort (buffer, 64 + i * 8) == 1;

      int itemId = Utility.getShort (buffer, 66 + i * 8);
      if (scenario == 3 && itemId >= 1000)
        itemId -= 1000;             // why?

      possessions.add (new Possession (itemId, equipped, cursed, identified));
    }

    experience = Utility.getWizLong (buffer, 124);

    characterLevel = Utility.getShort (buffer, 132);
    hpLeft = Utility.getShort (buffer, 134);
    hpMax = Utility.getShort (buffer, 136);

    armourClass = buffer[176];
  }

  // ---------------------------------------------------------------------------------//
  public void linkItems (List<ItemV1> itemList)
  // ---------------------------------------------------------------------------------//
  {
    for (Possession baggage : possessions)
    {
      baggage.item = itemList.get (baggage.itemId);
      assetValue += baggage.item.getCost ();
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
    text.append ("\n\nRace ............... " + race);
    text.append ("\nType ............... " + type);
    text.append ("\nAlignment .......... " + alignment);
    text.append ("\nStatus ............. " + status);
    text.append ("\nGold ............... " + String.format ("%,d", gold));
    text.append ("\nExperience ......... " + String.format ("%,d", experience));
    text.append ("\nNext level ......... " + String.format ("%,d", nextLevel));
    text.append ("\nLevel .............. " + characterLevel);
    text.append (
        "\nAge in weeks ....... " + String.format ("%,d  (%d)", ageInWeeks, (ageInWeeks / 52)));
    text.append ("\nHit points left .... " + hpLeft);
    text.append ("\nMaximum hits ....... " + hpMax);
    text.append ("\nArmour class ....... " + armourClass);
    text.append ("\nAsset value ........ " + String.format ("%,d", assetValue));

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
    for (Possession b : possessions)
      text.append ("\n" + b);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public void linkExperience (ExperienceLevel exp)
  // ---------------------------------------------------------------------------------//
  {
    nextLevel = exp.getExperiencePoints (characterLevel);
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
    return nextLevel;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isOut ()
  // ---------------------------------------------------------------------------------//
  {
    return inMaze;
  }

  // ---------------------------------------------------------------------------------//
  public String getType ()
  // ---------------------------------------------------------------------------------//
  {
    return type;
  }

  // ---------------------------------------------------------------------------------//
  public String getStatus ()
  // ---------------------------------------------------------------------------------//
  {
    return status;
  }

  // ---------------------------------------------------------------------------------//
  public String getRace ()
  // ---------------------------------------------------------------------------------//
  {
    return race;
  }

  // ---------------------------------------------------------------------------------//
  public String getAlignment ()
  // ---------------------------------------------------------------------------------//
  {
    return alignment;
  }

  // ---------------------------------------------------------------------------------//
  int[] getAttributes ()
  // ---------------------------------------------------------------------------------//
  {
    return attributes;
  }

  // ---------------------------------------------------------------------------------//
  public Iterator<Possession> getBaggage ()
  // ---------------------------------------------------------------------------------//
  {
    return possessions.iterator ();
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
  public class Possession
  // ---------------------------------------------------------------------------------//
  {
    public ItemV1 item;
    int itemId;
    public boolean cursed;
    public boolean equipped;
    public boolean identified;

    public Possession (int itemId, boolean equipped, boolean cursed, boolean identified)
    {
      this.itemId = itemId;
      this.equipped = equipped;
      this.identified = identified;
      this.cursed = cursed;
    }

    @Override
    public String toString ()
    {
      return String.format ("%s%-15s %,10d", equipped ? "*" : " ", item.getName (),
          item.getCost ());
    }
  }
}