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
  static int MAGE_SPELLS = 0;
  static int PRIEST_SPELLS = 1;

  public static final String[] spells = { "Halito", "Mogref", "Katino", "Dumapic", "Dilto", "Sopic",
      "Mahalito", "Molito", "Morlis", "Dalto", "Lahalito", "Mamorlis", "Makanito", "Madalto",
      "Lakanito", "Zilwan", "Masopic", "Haman", "Malor", "Mahaman", "Tiltowait",

      "Kalki", "Dios", "Badios", "Milwa", "Porfic", "Matu", "Calfo", "Manifo", "Montino", "Lomilwa",
      "Dialko", "Latumapic", "Bamatu", "Dial", "Badial", "Latumofis", "Maporfic", "Dialma",
      "Badialma", "Litokan", "Kandi", "Di", "Badi", "Lorto", "Madi", "Mabadi", "Loktofeit",
      "Malikto", "Kadorto" };

  int id;
  int nextCharacterId;
  CharacterParty party;

  public final boolean inMaze;
  public final Race race;
  public final CharacterClass characterClass;
  public final int age;
  public final CharacterStatus status;
  public final Alignment alignment;
  public final int[] attributes = new int[6];      // 0:18
  public final int[] saveVs = new int[5];          // 0:31
  public final long gold;

  public final int possessionsCount;
  public final List<Integer> possessionIds = new ArrayList<> (MAX_POSSESSIONS);
  public final List<ItemV4> possessions = new ArrayList<> (MAX_POSSESSIONS);

  public final long experience;
  public final int maxlevac;                       // max level armour class?
  public final int charlev;                        // character level?
  public final int hpLeft;
  public final int hpMax;

  public final boolean mysteryBit;                 // first bit in spellsKnown
  public final boolean[] spellsKnown = new boolean[50];
  public final int[][] spellAllowance = new int[2][7];

  public final int hpCalCmd;
  public final int armourClass;
  public final int healPts;

  public final boolean crithitm;
  public final int swingCount;
  public final Dice hpdamrc;                        // +184

  int unknown1;
  int unknown2;
  int unknown3;
  int unknown4;
  int unknown5;

  public enum Race
  {
    NORACE, HUMAN, ELF, DWARF, GNOME, HOBBIT
  }

  public enum Alignment
  {
    UNALIGN, GOOD, NEUTRAL, EVIL
  }

  public enum CharacterStatus
  {
    OK, AFRAID, ASLEEP, PLYZE, STONED, DEAD, ASHES, LOST
  }

  public enum CharacterClass
  {
    FIGHTER, MAGE, PRIEST, THIEF, BISHOP, SAMURAI, LORD, NINJA
  }

  public enum Status
  {
    OK, AFRAID, ASLEEP, PLYZE, STONED, DEAD, ASHES, LOST
  }

  // ---------------------------------------------------------------------------------//
  CharacterV4 (String name, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.id = id;
    scenario = 4;

    inMaze = Utility.getShort (buffer, 33) != 0;
    race = Race.values ()[Utility.getShort (buffer, 35)];
    characterClass = CharacterClass.values ()[Utility.getShort (buffer, 37)];
    age = 0;
    armourClass = Utility.signedShort (buffer, 39);

    status = CharacterStatus.values ()[Utility.getShort (buffer, 41)];
    alignment = Alignment.values ()[Utility.getShort (buffer, 43)];

    int attr1 = Utility.getShort (buffer, 45);
    int attr2 = Utility.getShort (buffer, 47);

    attributes[0] = attr1 & 0x001F;
    attributes[1] = (attr1 & 0x03E0) >>> 5;
    attributes[2] = attr1 & (0x7C00) >>> 10;
    attributes[3] = attr2 & 0x001F;
    attributes[4] = attr2 & (0x03E0) >>> 5;
    attributes[5] = attr2 & (0x7C00) >>> 10;

    gold = 0;

    unknown1 = Utility.getShort (buffer, 49);     // was luck/skill (4 bytes)
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
    charlev = Utility.getShort (buffer, 133);
    hpLeft = Utility.getShort (buffer, 135);
    hpMax = Utility.getShort (buffer, 137);

    mysteryBit = (buffer[139] & 0x01) == 1;
    int index = -1;                         // skip mystery bit
    for (int i = 139; i < 146; i++)
      for (int bit = 0; bit < 8; bit++)
      {
        if (((buffer[i] >>> bit) & 0x01) != 0)
          if (index >= 0)
            spellsKnown[index] = true;

        if (++index >= spells.length)
          break;
      }

    for (int i = 0; i < 7; i++)
    {
      spellAllowance[MAGE_SPELLS][i] = Utility.getShort (buffer, 147 + i * 2);
      spellAllowance[PRIEST_SPELLS][i] = Utility.getShort (buffer, 161 + i * 2);
    }

    hpCalCmd = Utility.signedShort (buffer, 175);
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
    return buffer[17] == 0 ? "" : HexFormatter.getPascalString (buffer, 17);
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
    text.append (String.format ("Level ? ........... %d%n", charlev));
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
      text.append ("\n");
      text.append (party);
    }

    text.append ("\n\n");
    text.append (HexFormatter.format (buffer, 1, buffer[0] & 0xFF));

    return text.toString ();
  }
}
