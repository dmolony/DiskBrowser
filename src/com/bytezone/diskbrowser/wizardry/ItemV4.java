package com.bytezone.diskbrowser.wizardry;

import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class ItemV4 extends Item
// -----------------------------------------------------------------------------------//
{

  // ---------------------------------------------------------------------------------//
  ItemV4 (String[] names, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (names[1], buffer);

    itemId = id;
    name = names[1];
    genericName = names[0];

    type = ObjectType.values ()[buffer[1]];
    alignment = Alignment.values ()[buffer[3]];
    cursed = Utility.getSignedShort (buffer, 5) == -1;
    special = Utility.getSignedShort (buffer, 7);
    changeTo = Utility.getShort (buffer, 9);            // decay #
    changeChance = Utility.getShort (buffer, 11);
    price = Utility.getWizLong (buffer, 13);
    boltac = Utility.getSignedShort (buffer, 19);
    spellPwr = Utility.getShort (buffer, 21);
    classUseFlags = Utility.getShort (buffer, 23);      // 8 flags

    healPts = Utility.getSignedShort (buffer, 25);
    flags2 = Utility.getShort (buffer, 27);             // 16 flags
    flags3 = Utility.getShort (buffer, 29);             // 16 flags
    armourClass = Utility.getSignedShort (buffer, 31);
    wephitmd = Utility.getSignedShort (buffer, 33);
    wephpdam = new Dice (buffer, 35);

    xtraSwing = Utility.getShort (buffer, 41);
    crithitm = Utility.getShort (buffer, 43) == 1;      // boolean
    flags1 = Utility.getShort (buffer, 45);             // 14 flags
  }

  // ---------------------------------------------------------------------------------//
  void link (List<ItemV4> items, List<String> spellNames)
  // ---------------------------------------------------------------------------------//
  {
    if (changeChance > 0)
      changeToItem = items.get (changeTo);

    if (spellPwr > 0)
      spellName = spellNames.get (spellPwr);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (super.getText ());

    //    text.append ("\n\n");
    //    text.append (HexFormatter.format (buffer));

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
