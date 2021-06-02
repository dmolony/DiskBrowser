package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
public class DebuggingAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
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
    firePropertyChange (e.getActionCommand (), null,
        ((JMenuItem) e.getSource ()).isSelected ());
  }
}