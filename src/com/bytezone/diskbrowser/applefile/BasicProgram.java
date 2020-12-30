package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.gui.BasicPreferences;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public abstract class BasicProgram extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  static BasicPreferences basicPreferences;     // set by MenuHandler

  // ---------------------------------------------------------------------------------//
  public static void setBasicPreferences (BasicPreferences basicPreferences)
  // ---------------------------------------------------------------------------------//
  {
    BasicProgram.basicPreferences = basicPreferences;
  }

  // ---------------------------------------------------------------------------------//
  public BasicProgram (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  boolean isHighBitSet (byte value)
  // ---------------------------------------------------------------------------------//
  {
    return (value & 0x80) != 0;
  }

  // ---------------------------------------------------------------------------------//
  boolean isControlCharacter (byte value)
  // ---------------------------------------------------------------------------------//
  {
    int val = value & 0xFF;
    return val > 0 && val < 32;
  }

  // ---------------------------------------------------------------------------------//
  boolean isDigit (byte value)
  // ---------------------------------------------------------------------------------//
  {
    return value >= 0x30 && value <= 0x39;
  }

  // ---------------------------------------------------------------------------------//
  boolean isLetter (byte value)
  // ---------------------------------------------------------------------------------//
  {
    return value >= 0x41 && value <= 0x5A;
  }

  // ---------------------------------------------------------------------------------//
  boolean isPossibleVariable (byte value)
  // ---------------------------------------------------------------------------------//
  {
    return isDigit (value) || isLetter (value) || value == Utility.ASCII_DOLLAR
        || value == Utility.ASCII_PERCENT;
  }
}
