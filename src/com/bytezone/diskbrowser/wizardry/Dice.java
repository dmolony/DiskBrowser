package com.bytezone.diskbrowser.wizardry;

class Dice
{
	int qty;
	int sides;
	int bonus;

	public Dice (byte[] buffer, int offset)
	{
		qty = buffer[offset];
		sides = buffer[offset + 2];
		bonus = buffer[offset + 4];
	}

	@Override
	public String toString ()
	{
		if (qty == 0)
			return "";
		StringBuilder text = new StringBuilder ();
		text.append (String.format ("%dd%d", qty, sides));
		if (bonus > 0)
			text.append ("+" + bonus);
		return text.toString ();
	}
}