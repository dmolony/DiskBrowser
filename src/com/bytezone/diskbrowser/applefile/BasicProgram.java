package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.gui.BasicPreferences;

public abstract class BasicProgram extends AbstractFile
{
  static final byte ASCII_QUOTE = 0x22;
  static final byte ASCII_COLON = 0x3A;
  static final byte ASCII_SEMI_COLON = 0x3B;
  static final byte ASCII_CARET = 0x5E;

  static BasicPreferences basicPreferences;

  public static void setBasicPreferences (BasicPreferences basicPreferences)
  {
    ApplesoftBasicProgram.basicPreferences = basicPreferences;
  }

  public BasicProgram (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  boolean isToken (byte value)
  {
    return (value & 0x80) != 0;
  }

  boolean isControlCharacter (byte value)
  {
    return (value & 0xFF) < 32;
  }

  boolean isDigit (byte value)
  {
    return value >= 48 && value <= 57;
  }
}
