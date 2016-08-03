package com.bytezone.diskbrowser.gui;

import java.awt.Dimension;
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
    //    System.out.printf ("Saving x:%d, y:%d, w:%d, h:%d%n", frame.getX (), frame.getY (),
    //                       frame.getWidth (), frame.getHeight ());
  }

  public boolean restoreWindow ()
  {
    int x = prefs.getInt (key + "X", -1);
    int y = prefs.getInt (key + "Y", -1);
    int height = prefs.getInt (key + "Height", -1);
    int width = prefs.getInt (key + "Width", -1);

    Dimension screen = java.awt.Toolkit.getDefaultToolkit ().getScreenSize ();

    if (width < 0)                // nothing to restore
    {
      frame.setLocation (100, 100);
      frame.setSize (1000, 600);
      frame.setLocationRelativeTo (null);             // centre
      //      System.out.printf ("Creating x:%d, y:%d, w:%d, h:%d%n", x, y, width, height);
      return false;
    }

    //    System.out.printf ("w:%d, sw:%f%n", width, screen.getWidth ());
    if (width > screen.getWidth () - 15)
      width = (int) (screen.getWidth () - 15);

    frame.setSize (width, height);
    frame.setLocation (x, y);
    //    System.out.printf ("Restoring x:%d, y:%d, w:%d, h:%d%n", x, y, width, height);

    return true;
  }
}