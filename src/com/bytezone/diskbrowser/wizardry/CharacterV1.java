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

  int[] mageSpells = new int[7];
  int[] priestSpells = new int[7];

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

    checkKnownSpells (buffer, 138);

    for (int i = 0; i < 7; i++)
      mageSpells[i] = buffer[146 + i * 2];

    for (int i = 0; i < 7; i++)
      priestSpells[i] = buffer[160 + i * 2];

    armourClass = buffer[176];
  }

  // ---------------------------------------------------------------------------------//
  public void link (List<ItemV1> itemList, List<Spell> spellList,
      List<ExperienceLevel> experienceLevels)
  // ---------------------------------------------------------------------------------//
  {
    for (Possession baggage : possessions)
    {
      baggage.item = itemList.get (baggage.itemId);
      assetValue += baggage.item.getCost ();
    }

    for (int i = 0; i < spellsKnown.length; i++)
      if (spellsKnown[i])
        spellBook.add (spellList.get (i));

    nextLevel = experienceLevels.get (typeInt).getExperiencePoints (characterLevel + 1);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Character name ..... %s%n", getName ()));
    text.append ("\nRace ............... " + race);
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

    text.append ("\n\nMage spell points ..");
    for (int i = 0; i < mageSpells.length; i++)
      text.append (" " + mageSpells[i]);

    text.append ("\nPriest spell points ");
    for (int i = 0; i < priestSpells.length; i++)
      text.append (" " + priestSpells[i]);

    text.append ("\n\nSpells :");
    for (Spell s : spellBook)
      text.append ("\n" + s);

    text.append ("\n\nItems :");
    for (Possession b : possessions)
      text.append ("\n" + b);

    return text.toString ();
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