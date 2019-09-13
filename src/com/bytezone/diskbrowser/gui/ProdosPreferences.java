package com.bytezone.diskbrowser.gui;

public class ProdosPreferences
{
  public boolean sortDirectories;

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Sort directies ........ %s%n", sortDirectories));

    return text.toString ();
  }
}
