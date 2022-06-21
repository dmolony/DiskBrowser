package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public abstract class Character extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  static int MAGE_SPELLS = 0;
  static int PRIEST_SPELLS = 1;

  static String[] races = { "No race", "Human", "Elf", "Dwarf", "Gnome", "Hobbit" };
  static String[] alignments = { "Unalign", "Good", "Neutral", "Evil" };
  static String[] types =
      { "Fighter", "Mage", "Priest", "Thief", "Bishop", "Samurai", "Lord", "Ninja" };
  static String[] statuses =
      { "OK", "Afraid", "Asleep", "Paralyze", "Stoned", "Dead", "Ashes", "Lost" };
  static char[] awardsText = ">!$#&*<?BCPKODG@".toCharArray ();

  int scenario;

  public boolean inMaze;
  public int hpLeft;
  public int hpMax;
  public int armourClass;
  public final int[] attributes = new int[6];      // 0:18
  public final int[] saveVs = new int[5];          // 0:31
  public int characterLevel;
  public int possessionsCount;

  public boolean mysteryBit;                 // first bit in spellsKnown
  public final boolean[] spellsKnown = new boolean[50];
  public final int[][] spellAllowance = new int[2][7];

  public enum Race
  {
    NORACE, HUMAN, ELF, DWARF, GNOME, HOBBIT
  }

  public enum Alignment
  {
    UNALIGN, GOOD, NEUTRAL, EVIL
  }

  public enum CharacterStatus
  {
    OK, AFRAID, ASLEEP, PLYZE, STONED, DEAD, ASHES, LOST
  }

  public enum CharacterClass
  {
    FIGHTER, MAGE, PRIEST, THIEF, BISHOP, SAMURAI, LORD, NINJA
  }

  // ---------------------------------------------------------------------------------//
  Character (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  void get3x5Bits (int[] attributes, int ptr, int value)
  // ---------------------------------------------------------------------------------//
  {
    attributes[ptr] = value & 0x001F;
    attributes[ptr + 1] = (value & 0x03E0) >>> 5;
    attributes[ptr + 2] = (value & 0x7C00) >>> 10;
  }

  // ---------------------------------------------------------------------------------//
  void get2x5Bits (int[] attributes, int ptr, int value)
  // ---------------------------------------------------------------------------------//
  {
    attributes[ptr] = value & 0x001F;
    attributes[ptr + 1] = (value & 0x03E0) >>> 5;
  }

  // ---------------------------------------------------------------------------------//
  protected void checkKnownSpells (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < 7; i++)
    {
      spellAllowance[MAGE_SPELLS][i] = Utility.getShort (buffer, ptr + 8 + i * 2);
      spellAllowance[PRIEST_SPELLS][i] = Utility.getShort (buffer, ptr + 22 + i * 2);
    }

    int bit = 1;                  // skip first bit
    int val = buffer[ptr];
    mysteryBit = (val & 0x01) == 1;

    for (int i = 0; i < spellsKnown.length; i++)
    {
      if (bit == 8)
      {
        val = buffer[++ptr];
        bit = 0;
      }
      spellsKnown[i] = ((val >>> bit++) & 0x01) != 0;
    }
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
