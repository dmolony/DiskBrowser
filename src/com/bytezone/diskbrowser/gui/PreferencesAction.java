package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;

// not currently used
public class PreferencesAction extends DefaultAction
{
  JFrame owner;
  Preferences prefs;

  public PreferencesAction (JFrame owner, Preferences prefs)
  {
    super ("Preferences...", "Set preferences", "/com/bytezone/diskbrowser/icons/");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt P"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_P);

    setIcon (Action.LARGE_ICON_KEY, "script_gear_32.png");
    this.owner = owner;
    this.prefs = prefs;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    prefs ();
  }

  public void prefs ()
  {
    new PreferencesDialog (owner, prefs);
  }
}