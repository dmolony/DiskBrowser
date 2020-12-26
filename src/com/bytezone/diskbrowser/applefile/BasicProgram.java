package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.gui.BasicPreferences;

// -----------------------------------------------------------------------------------//
public abstract class BasicProgram extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  static final byte ASCII_QUOTE = 0x22;
  static final byte ASCII_DOLLAR = 0x24;
  static final byte ASCII_PERCENT = 0x25;
  static final byte ASCII_LEFT_BRACKET = 0x28;
  static final byte ASCII_COLON = 0x3A;
  static final byte ASCII_SEMI_COLON = 0x3B;
  static final byte ASCII_CARET = 0x5E;

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

  boolean isPossibleVariable (byte value)
  {
    return isDigit (value) || isLetter (value) || value == ASCII_DOLLAR
        || value == ASCII_PERCENT;
  }
}
