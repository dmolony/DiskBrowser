package com.bytezone.diskbrowser.gui;

import java.util.prefs.Preferences;

import javax.swing.JFrame;

public class WindowSaver
{
  private final Preferences prefs;
  private final JFrame frame;
  private final String key;

  public WindowSaver (Preferences prefs, JFrame frame, String key)
  {
    this.prefs = prefs;
    this.frame = frame;
    this.key = key;
  }

  public void saveWindow ()
  {
    prefs.putInt (key + "X", frame.getX ());
    prefs.putInt (key + "Y", frame.getY ());
    prefs.putInt (key + "Height", frame.getHeight ());
    prefs.putInt (key + "Width", frame.getWidth ());
  }

  public boolean restoreWindow ()
  {
    int x = prefs.getInt (key + "X", -1);
    int y = prefs.getInt (key + "Y", -1);
    int height = prefs.getInt (key + "Height", -1);
    int width = prefs.getInt (key + "Width", -1);

    if (width < 0)                // nothing to restore
    {
      frame.setLocationRelativeTo (null);             // centre
      //      frame.centerOnScreen ();
      return false;
    }

    //    frame.setX (x);
    //    frame.setY (y);
    //    frame.setHeight (height);
    //    frame.setWidth (width);
    frame.setLocation (x, y);
    frame.setSize (width, height);

    return true;
  }
}