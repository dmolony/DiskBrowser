package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.gui.BasicPreferences;

public abstract class BasicProgram extends AbstractFile
{
  static final byte ASCII_QUOTE = 0x22;
  static final byte ASCII_COLON = 0x3A;
  static final byte ASCII_SEMI_COLON = 0x3B;
  static final byte ASCII_CARET = 0x5E;

  static BasicPreferences basicPreferences;     // set by MenuHandler

  public static void setBasicPreferences (BasicPreferences basicPreferences)
  {
    BasicProgram.basicPreferences = basicPreferences;
  }

  public BasicProgram (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  boolean isHighBitSet (byte value)
  {
    return (value & 0x80) != 0;
  }

  boolean isControlCharacter (byte value)
  {
    int val = value & 0xFF;
    return val > 0 && val < 32;
  }

  boolean isDigit (byte value)
  {
    return value >= 48 && value <= 57;
  }
}
