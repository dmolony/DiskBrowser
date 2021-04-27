package com.bytezone.diskbrowser.gui;

// -----------------------------------------------------------------------------------//
public class TextPreferences
//-----------------------------------------------------------------------------------//
{
  public boolean showTextOffsets;
  public boolean showHeader = true;
  public boolean merlinFormat = true;

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Show offsets .......... %s%n", showTextOffsets));
    text.append (String.format ("Show header ........... %s%n", showHeader));
    text.append (String.format ("Show .S as Merlin ..... %s", merlinFormat));

    return text.toString ();
  }
}
