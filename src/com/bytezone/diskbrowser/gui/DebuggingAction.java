package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
public class DebuggingAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  List<DebugListener> listeners = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public DebuggingAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Debugging");
    putValue (Action.SHORT_DESCRIPTION, "Show debugging information");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("meta D"));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    for (DebugListener listener : listeners)
      listener.setDebug (((JMenuItem) e.getSource ()).isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  public void addDebugListener (DebugListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  public interface DebugListener
  // ---------------------------------------------------------------------------------//
  {
    public void setDebug (boolean value);
  }
}