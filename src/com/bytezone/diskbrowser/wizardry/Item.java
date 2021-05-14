package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class Item extends AbstractFile implements Comparable<Item>
// -----------------------------------------------------------------------------------//
{
  public final int itemID;
  private final int type;
  private final long cost;
  public int partyOwns;
  String genericName;
  static int counter = 0;
  public final Dice damage;
  public final int armourClass;
  public final int speed;

  // ---------------------------------------------------------------------------------//
  Item (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
    itemID = counter++;
    type = buffer[32];
    cost = Utility.unsignedShort (buffer, 44) + Utility.unsignedShort (buffer, 46) * 10000
        + Utility.unsignedShort (buffer, 48) * 100000000L;
    genericName = HexFormatter.getPascalString (buffer, 16);
    damage = new Dice (buffer, 66);
    armourClass = buffer[62];
    speed = buffer[72];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Name ......... : " + name);
    //		int length = HexFormatter.intValue (buffer[16]);
    text.append ("\nGeneric name . : " + genericName);
    text.append ("\nType ......... : " + type);
    text.append ("\nCost ......... : " + cost);
    text.append ("\nArmour class . : " + armourClass);
    text.append ("\nDamage ....... : " + damage);
    text.append ("\nSpeed ........ : " + speed);
    text.append ("\nCursed? ...... : " + isCursed ());
    int stock = getStockOnHand ();
    text.append ("\nStock on hand  : " + stock);
    if (stock < 0)
      text.append (" (always in stock)");

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public int getType ()
  // ---------------------------------------------------------------------------------//
  {
    return type;
  }

  //	public int getArmourClass ()
  //	{
  //		return buffer[62];
  //	}

  //	public int getSpeed ()
  //	{
  //		return HexFormatter.intValue (buffer[72]);
  //	}

  // ---------------------------------------------------------------------------------//
  public long getCost ()
  // ---------------------------------------------------------------------------------//
  {
    return cost;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isCursed ()
  // ---------------------------------------------------------------------------------//
  {
    return buffer[36] != 0;
  }

  // ---------------------------------------------------------------------------------//
  public int getStockOnHand ()
  // ---------------------------------------------------------------------------------//
  {
    if (buffer[50] == -1 && buffer[51] == -1)
      return -1;

    return Utility.unsignedShort (buffer, 50);
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
    line.append (String.format ("%-16s", name));
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
    StringBuilder line = new StringBuilder (String.format ("%3d %-16s", itemID, name));
    int lo = block == 0 ? 32 : block == 1 ? 46 : 70;
    int hi = lo + 24;
    if (hi > buffer.length)
      hi = buffer.length;
    for (int i = lo; i < hi; i++)
      line.append (String.format ("%02X ", buffer[i]));
    return line.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int compareTo (Item otherItem)
  // ---------------------------------------------------------------------------------//
  {
    Item item = otherItem;
    return this.type - item.type;
  }
}