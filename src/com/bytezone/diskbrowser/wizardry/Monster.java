package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// -----------------------------------------------------------------------------------//
public abstract class Monster extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  public static final String[] monsterClass = { "Fighter", "Mage", "Priest", "Thief", "Midget",
      "Giant", "Mythical", "Dragon", "Animal", "Were", "Undead", "Demon", "Insect", "Enchanted" };
  protected final String[] breathValues =
      { "None", "Fire", "Frost", "Poison", "Level drain", "Stoning", "Magic" };

  public int monsterID;

  public String genericName;
  public String genericNamePlural;
  public String namePlural;

  public Dice groupSize;
  public Dice hitPoints;
  int type;
  public int armourClass;
  public int recsn;
  List<Dice> damage = new ArrayList<> ();

  public int mageSpellLevel;
  public int priestSpellLevel;

  int levelDrain;
  int healPts;
  int breathe;
  int unaffect;
  int resistance;
  int abilities;

  // ---------------------------------------------------------------------------------//
  Monster (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("ID .............. %d%n%n", monsterID));
    text.append (String.format ("Name ............ %s%n", name));
    text.append (String.format ("Name plural ..... %s%n", namePlural));
    text.append (String.format ("Generic name .... %s%n", genericName));
    text.append (String.format ("Generic name pl . %s%n%n", genericNamePlural));

    text.append (String.format ("Type ............ %2d  %s%n", type, monsterClass[type]));
    text.append (String.format ("Armour class .... %d%n", armourClass));
    text.append (String.format ("Group size ...... %s%n", groupSize));
    text.append (String.format ("Hit points ...... %s%n%n", hitPoints));

    text.append (String.format ("# damage ........ %d%n", recsn));
    text.append (String.format ("Damage .......... %s%n%n", getDamage ()));

    text.append (String.format ("Mage level ...... %d%n", mageSpellLevel));
    text.append (String.format ("Priest level .... %d%n%n", priestSpellLevel));

    text.append (String.format ("Level drain ..... %d%n", levelDrain));
    text.append (String.format ("Heal pts ........ %d%n", healPts));
    text.append (String.format ("Breathe ......... %d  %s%n", breathe, breathValues[breathe]));
    text.append (String.format ("Magic resist .... %d%% %n%n", unaffect));

    text.append (
        String.format ("Resistance ...... %s%n", String.format ("%3d  %<02X", resistance)));
    text.append (String.format ("Abilities ....... %s%n", String.format ("%3d  %<02X", abilities)));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public String getDamage ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (Dice d : damage)
      text.append (d + ", ");

    if (text.length () > 0)
    {
      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }
}
