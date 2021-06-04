package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
class HideLayoutAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public HideLayoutAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Show disk layout panel");

    putValue (Action.SHORT_DESCRIPTION, "Show/hide the disk layout panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt D"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_D);
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