package com.bytezone.diskbrowser.wizardry;

import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class ItemV1 extends Item // implements Comparable<ItemV1>
// -----------------------------------------------------------------------------------//
{
  //  public int partyOwns;
  //  static int counter = 0;

  // ---------------------------------------------------------------------------------//
  ItemV1 (int itemId, String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    //    itemId = counter++;
    this.itemId = itemId;
    genericName = HexFormatter.getPascalString (buffer, 16);

    type = ObjectType.values ()[buffer[32]];
    alignment = Alignment.values ()[buffer[34]];
    cursed = Utility.getSignedShort (buffer, 36) == -1;
    special = Utility.getSignedShort (buffer, 38);
    changeTo = Utility.getShort (buffer, 40);            // decay #
    changeChance = Utility.getShort (buffer, 42);
    price = Utility.getWizLong (buffer, 44);
    boltac = Utility.getSignedShort (buffer, 50);
    spellPwr = Utility.getShort (buffer, 52);
    classUseFlags = Utility.getShort (buffer, 54);       // 8 flags

    healPts = Utility.getSignedShort (buffer, 56);
    flags2 = Utility.getShort (buffer, 58);       // 16 flags
    flags3 = Utility.getShort (buffer, 60);       // 16 flags
    armourClass = Utility.getSignedShort (buffer, 62);
    wephitmd = Utility.getSignedShort (buffer, 64);
    wephpdam = new Dice (buffer, 66);                    // Dice
    xtraSwing = Utility.getShort (buffer, 72);
    crithitm = Utility.getShort (buffer, 74) == 1;       // boolean
    flags1 = Utility.getShort (buffer, 76);        // 14 flags
  }

  // ---------------------------------------------------------------------------------//
  void link (List<ItemV1> items, List<Spell> spells)
  // ---------------------------------------------------------------------------------//
  {
    if (changeChance > 0)
      changeToItem = items.get (changeTo);

    if (spellPwr > 0)
      spell = spells.get (spellPwr - 1);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (super.getText ());

    //    int stock = getStockOnHand ();
    //    text.append ("\nStock on hand  : " + stock);
    //    if (stock < 0)
    //      text.append (" (always in stock)");

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public long getCost ()
  // ---------------------------------------------------------------------------------//
  {
    return price;
  }

  // ---------------------------------------------------------------------------------//
  public int getStockOnHand ()
  // ---------------------------------------------------------------------------------//
  {
    //    if (buffer[50] == -1 && buffer[51] == -1)
    //      return -1;
    //
    //    return Utility.getShort (buffer, 50);
    return boltac;
  }

  // ---------------------------------------------------------------------------------//
  public boolean canUse (int type2)
  // ---------------------------------------------------------------------------------//
  {
    int users = buffer[54] & 0xFF;
    return ((users >>> type2) & 1) == 1;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = new StringBuilder ();
    line.append (String.format ("%-16s", getName ()));
    if (buffer[36] == -1)
      line.append ("(c) ");
    else
      line.append ("    ");
    line.append (String.format ("%02X ", buffer[62]));
    line.append (String.format ("%02X ", buffer[34]));
    line.append (String.format ("%02X %02X", buffer[50], buffer[51]));

    //		if (buffer[50] == -1 && buffer[51] == -1)
    //			line.append ("* ");
    //		else
    //			line.append (HexFormatter.intValue (buffer[50], buffer[51]) + " ");

    for (int i = 38; i < 44; i++)
      line.append (HexFormatter.format2 (buffer[i]) + " ");
    for (int i = 48; i < 50; i++)
      line.append (HexFormatter.format2 (buffer[i]) + " ");
    for (int i = 52; i < 62; i++)
      line.append (HexFormatter.format2 (buffer[i]) + " ");
    //		for (int i = 64; i < 78; i++)
    //			line.append (HexFormatter.format2 (buffer[i]) + " ");

    return line.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public String getDump (int block)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = new StringBuilder (String.format ("%3d %-16s", itemId, getName ()));

    int lo = block == 0 ? 32 : block == 1 ? 56 : 80;
    int hi = lo + 24;
    if (hi > buffer.length)
      hi = buffer.length;

    for (int i = lo; i < hi; i++)
      line.append (String.format ("%02X ", buffer[i]));

    return line.toString ();
  }

  // ---------------------------------------------------------------------------------//
  //  @Override
  //  public int compareTo (ItemV1 otherItem)
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    ItemV1 item = otherItem;
  //    return this.type - item.type;
  //  }
}