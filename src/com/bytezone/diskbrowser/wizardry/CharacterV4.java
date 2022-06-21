package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class CharacterV4 extends Character
// -----------------------------------------------------------------------------------//
{
  private static int MAX_POSSESSIONS = 8;
  private static int MAX_SPELLS = 50;
  static int MAGE_SPELLS = 0;
  static int PRIEST_SPELLS = 1;

  int id;
  int nextCharacterId;
  CharacterParty party;
  String partialSlogan;

  public final Race race;
  public final CharacterClass characterClass;
  public final int age;
  public final CharacterStatus status;
  public final Alignment alignment;
  public final long gold;

  public final List<Integer> possessionIds = new ArrayList<> (MAX_POSSESSIONS);
  public final List<ItemV4> possessions = new ArrayList<> (MAX_POSSESSIONS);

  public final long experience;
  public final int maxlevac;                       // max level armour class?

  public final int hpCalCmd;
  public final int healPts;

  public final boolean crithitm;
  public final int swingCount;
  public final Dice hpdamrc;                        // +184

  int unknown1;
  int unknown2;
  int unknown3;
  int unknown4;
  int unknown5;

  // ---------------------------------------------------------------------------------//
  CharacterV4 (String name, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.id = id;
    scenario = 4;

    partialSlogan = buffer[17] == 0 ? "" : HexFormatter.getPascalString (buffer, 17);

    inMaze = Utility.getShort (buffer, 33) != 0;
    race = Race.values ()[Utility.getShort (buffer, 35)];
    characterClass = CharacterClass.values ()[Utility.getShort (buffer, 37)];
    age = 0;
    armourClass = Utility.getSignedShort (buffer, 39);

    status = CharacterStatus.values ()[Utility.getShort (buffer, 41)];
    alignment = Alignment.values ()[Utility.getShort (buffer, 43)];

    get3x5Bits (attributes, 0, Utility.getShort (buffer, 45));
    get3x5Bits (attributes, 3, Utility.getShort (buffer, 47));

    gold = 0;

    unknown1 = Utility.getShort (buffer, 49);     // was saveVs (4 bytes)
    unknown2 = Utility.getShort (buffer, 51);
    unknown3 = Utility.getShort (buffer, 53);     // was gold (6 bytes)
    unknown4 = Utility.getShort (buffer, 55);
    unknown5 = Utility.getShort (buffer, 57);

    possessionsCount = Utility.getShort (buffer, 59);

    for (int i = 0; i < possessionsCount; i++)
    {
      //      boolean equipped = Utility.getShort (buffer, 61 + i * 8) == 1;
      //      boolean cursed = Utility.getShort (buffer, 63 + i * 8) == 1;
      //      boolean identified = Utility.getShort (buffer, 65 + i * 8) == 1;
      int itemNo = Utility.getShort (buffer, 67 + i * 8);
      //      Possession p = new Possession (itemNo, equipped, cursed, identified);
      possessionIds.add (itemNo);
    }

    experience = 0;
    nextCharacterId = Utility.getShort (buffer, 125);
    maxlevac = Utility.getShort (buffer, 131);

    characterLevel = Utility.getShort (buffer, 133);
    hpLeft = Utility.getShort (buffer, 135);
    hpMax = Utility.getShort (buffer, 137);

    checkKnownSpells (buffer, 139);

    for (int i = 0; i < 7; i++)
    {
      spellAllowance[MAGE_SPELLS][i] = Utility.getShort (buffer, 147 + i * 2);
      spellAllowance[PRIEST_SPELLS][i] = Utility.getShort (buffer, 161 + i * 2);
    }

    hpCalCmd = Utility.getSignedShort (buffer, 175);
    //    armourClass = Utility.getSignedShort (buffer, 177);   // see offset 39
    healPts = Utility.getShort (buffer, 179);

    crithitm = Utility.getShort (buffer, 181) == 1;
    swingCount = Utility.getShort (buffer, 183);
    hpdamrc = new Dice (buffer, 185);

  }

  // ---------------------------------------------------------------------------------//
  void addPossessions (List<ItemV4> items)
  // ---------------------------------------------------------------------------------//
  {
    for (int itemId : possessionIds)
    {
      possessions.add (items.get (itemId));
    }
  }

  // ---------------------------------------------------------------------------------//
  void setParty (CharacterParty party)
  // ---------------------------------------------------------------------------------//
  {
    this.party = party;
  }

  // ---------------------------------------------------------------------------------//
  boolean isInParty ()
  // ---------------------------------------------------------------------------------//
  {
    return party != null;
  }

  // ---------------------------------------------------------------------------------//
  String getPartialSlogan ()
  // ---------------------------------------------------------------------------------//
  {
    //    return buffer[17] == 0 ? "" : HexFormatter.getPascalString (buffer, 17);
    return partialSlogan;
  }

  // ---------------------------------------------------------------------------------//
  String getAttributeString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < attributes.length; i++)
      text.append (String.format ("%02d/", attributes[i]));
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  String getSpellsString (int which)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int total = 0;
    for (int i = 0; i < spellAllowance[which].length; i++)
      total += spellAllowance[which][i];

    if (total == 0)
      return "";

    for (int i = 0; i < spellAllowance[which].length; i++)
      text.append (String.format ("%d/", spellAllowance[which][i]));
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  String getTypeString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%1.1s-%3.3s", alignment, characterClass);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Id ................ %d%n", id));
    text.append (String.format ("Name .............. %s%n", name));
    text.append (String.format ("Race .............. %s%n", race));
    text.append (String.format ("Character class ... %s%n", characterClass));
    text.append (String.format ("Alignment ......... %s%n", alignment));
    text.append (String.format ("Status ............ %s%n", status));
    text.append (String.format ("Level ? ........... %d%n", characterLevel));
    text.append (String.format ("Hit points ........ %d/%d%n", hpLeft, hpMax));
    text.append (String.format ("Armour class ...... %d%n", armourClass));
    text.append (String.format ("Attributes ........ %s%n", getAttributeString ()));
    text.append (String.format ("Mage spells ....... %s%n", getSpellsString (MAGE_SPELLS)));
    text.append (String.format ("Priest spells ..... %s%n", getSpellsString (PRIEST_SPELLS)));

    if (possessionsCount > 0)
    {
      text.append ("\nPossessions:\n");
      for (ItemV4 item : possessions)
        text.append ("  " + item + "\n");
    }

    if (!party.slogan.isEmpty () || party.characters.size () > 1)
    {
      for (int i = possessionsCount; i < 9; i++)
        text.append ("\n");
      text.append (party);
    }

    //    text.append ("\n\n");
    //    text.append (HexFormatter.format (buffer, 1, buffer[0] & 0xFF));

    return text.toString ();
  }
}
