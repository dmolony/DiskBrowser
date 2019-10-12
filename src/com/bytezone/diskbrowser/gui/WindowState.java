package com.bytezone.diskbrowser.gui;

import java.awt.Dimension;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

class WindowState
{
  private static final String PREF_WINDOW_WIDTH = "WindowWidth";
  private static final String PREF_WINDOW_HEIGHT = "WindowHeight";
  private static final String PREF_WINDOW_STATE = "WindowExtendedState";

  public Preferences preferences;

  public WindowState (Preferences preferences)
  {
    this.preferences = preferences;
  }

  public void clear ()
  {
    try
    {
      preferences.clear ();
      System.out.println ("Preferences cleared");
    }
    catch (BackingStoreException e)
    {
      e.printStackTrace ();
    }
  }

  public Dimension getWindowSize (int defaultWidth, int defaultHeight)
  {
    int width = preferences.getInt (PREF_WINDOW_WIDTH, defaultWidth);
    int height = preferences.getInt (PREF_WINDOW_HEIGHT, defaultHeight);
    return new Dimension (width, height);
  }

  public int getExtendedState (int defaultState)
  {
    return preferences.getInt (PREF_WINDOW_STATE, defaultState);
  }

  public void save (JFrame window)
  {
    preferences.putInt (PREF_WINDOW_WIDTH, window.getWidth ());
    preferences.putInt (PREF_WINDOW_HEIGHT, window.getHeight ());
    preferences.putInt (PREF_WINDOW_STATE, window.getExtendedState ());
  }
}