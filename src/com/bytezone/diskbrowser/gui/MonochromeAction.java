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
class MonochromeAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  List<MonochromeListener> listeners = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  MonochromeAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Monochrome");
    putValue (Action.SHORT_DESCRIPTION, "Display image in monochrome or color");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt M"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_M);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    for (MonochromeListener listener : listeners)
      listener.setMonochrome (((JMenuItem) e.getSource ()).isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  public void addMonochromeListener (MonochromeListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  public interface MonochromeListener
  // ---------------------------------------------------------------------------------//
  {
    public void setMonochrome (boolean value);
  }
}