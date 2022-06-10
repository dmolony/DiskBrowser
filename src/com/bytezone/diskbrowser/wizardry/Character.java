package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.Utility;

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
  static char[] awardsText = ">!$#&*<?BCPKODG@".toCharArray ();

  int scenario;

  // ---------------------------------------------------------------------------------//
  Character (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  public String getAwardString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int awards = Utility.getShort (buffer, 206);

    for (int i = 0; i < 16; i++)
    {
      if ((awards & 0x01) != 0)
        text.append (awardsText[i]);
      awards >>>= 1;
    }

    return text.toString ();
  }
}
