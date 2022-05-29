package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// -----------------------------------------------------------------------------------//
public abstract class Character extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  static String[] races = { "No race", "Human", "Elf", "Dwarf", "Gnome", "Hobbit" };
  static String[] alignments = { "Unalign", "Good", "Neutral", "Evil" };
  static String[] types =
      { "Fighter", "Mage", "Priest", "Thief", "Bishop", "Samurai", "Lord", "Ninja" };
  static String[] statuses =
      { "OK", "Afraid", "Asleep", "Paralyze", "Stoned", "Dead", "Ashes", "Lost" };

  int scenario;

  // ---------------------------------------------------------------------------------//
  Character (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }
}
