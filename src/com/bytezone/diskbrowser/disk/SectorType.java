package com.bytezone.diskbrowser.disk;

import java.awt.Color;

public class SectorType
{
	public final String name;
	public final Color colour;

	public SectorType (String name, Color colour)
	{
		this.name = name;
		this.colour = colour;
	}

	@Override
	public String toString ()
	{
		return String.format ("[SectorType : %s, %s]", name, colour);
	}
}