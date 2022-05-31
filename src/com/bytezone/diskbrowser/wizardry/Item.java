package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class Item extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  int itemId;
  String genericName;
  protected long price;
  public Dice wephpdam;
  public int armourClass;
  public int xtraSwing;
  boolean cursed;
  int changeTo;
  int changeChance;
  int special;
  int boltac;
  int spellPwr;
  int classUseFlags;
  int healPts;
  int wepvstyFlags;
  int wepvsty2Flags;
  int wepvsty3Flags;
  int wephitmd;
  boolean crithitm;

  ObjectType type;
  Alignment alignment;

  Item changeToItem;
  Spell spell;

  public enum Alignment
  {
    UNALIGN, GOOD, NEUTRAL, EVIL
  }

  public enum ObjectType
  {
    WEAPON, ARMOR, SHIELD, HELMET, GAUNTLET, SPECIAL, MISC
  }

  // ---------------------------------------------------------------------------------//
  Item (String name, byte[] buffer)
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

    text.append (String.format ("ID ............... %s%n%n", itemId));
    text.append (String.format ("Name ............. %s%n", name));
    text.append (String.format ("Generic name ..... %s%n", genericName));

    text.append (String.format ("Cost ............. %,d%n", price));
    text.append (String.format ("Damage ........... %s%n", wephpdam));
    text.append (String.format ("Hit mod .......... %d%n", wephitmd));
    text.append (String.format ("Critical hit ..... %s%n", crithitm));

    text.append (String.format ("Type ............. %s%n", type));
    text.append (String.format ("Alignment ........ %s%n", alignment));
    text.append (String.format ("Armour class ..... %d%n", armourClass));
    text.append (String.format ("Speed ............ %d%n", xtraSwing));
    text.append (String.format ("Cursed? .......... %s%n", cursed));

    String changeItemName = changeToItem == null ? "" : changeToItem.getName ();
    text.append (String.format ("Decay odds ....... %d%%%n", changeChance));
    text.append (String.format ("Decay to ......... %s%n", changeItemName));

    text.append (String.format ("Special .......... %d%n", special));
    text.append (String.format ("Boltac ........... %d%n", boltac));

    String spellName = spell == null ? "" : spell.getName ();
    text.append (String.format ("Spell ............ %s%n", spellName));

    text.append (String.format ("Heal ............. %d%n", healPts));
    text.append (String.format ("Class use ........ %d%n", classUseFlags));
    text.append (String.format ("Flags ............ %d%n", wepvstyFlags));
    text.append (String.format ("Flags2 ........... %d%n", wepvsty2Flags));
    text.append (String.format ("Flags3 ........... %d%n", wepvsty3Flags));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  int getWizLong (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    int low = Utility.getShort (buffer, offset);
    int mid = Utility.getShort (buffer, offset + 2);
    int high = Utility.getShort (buffer, offset + 4);

    return high * 100000000 + mid * 10000 + low;
  }
}
