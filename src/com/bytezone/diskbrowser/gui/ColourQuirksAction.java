package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
public class ColourQuirksAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  List<ColourQuirksListener> listeners = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public ColourQuirksAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Smear HGR");
    putValue (Action.SHORT_DESCRIPTION, "Display pixels like a TV screen");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt Q"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_Q);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    for (ColourQuirksListener listener : listeners)
      listener.setColourQuirks (((JMenuItem) e.getSource ()).isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  public void addColourQuirksListener (ColourQuirksListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  public interface ColourQuirksListener
  // ---------------------------------------------------------------------------------//
  {
    public void setColourQuirks (boolean value);
  }
}