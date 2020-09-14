package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.gui.TextPreferences;

public abstract class TextFile extends AbstractFile
{
  static TextPreferences textPreferences;     // set by MenuHandler

  // ---------------------------------------------------------------------------------//
  public TextFile (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  public static void setTextPreferences (TextPreferences textPreferences)
  // ---------------------------------------------------------------------------------//
  {
    TextFile.textPreferences = textPreferences;
  }
}
