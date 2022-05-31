package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.utilities.HexFormatter;

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

    price = getWizLong (buffer, 13);
    wephpdam = new Dice (buffer, 35);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (super.getText ());

    text.append ("\n\n");
    text.append (HexFormatter.format (buffer));

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
