package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.gui.BasicPreferences;

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
}
