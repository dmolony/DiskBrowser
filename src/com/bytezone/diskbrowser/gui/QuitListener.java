package com.bytezone.diskbrowser.gui;

import java.util.prefs.Preferences;

// -----------------------------------------------------------------------------------//
public interface QuitListener
// -----------------------------------------------------------------------------------//
{
  public void quit (Preferences preferences);

  public void restore (Preferences preferences);
}
