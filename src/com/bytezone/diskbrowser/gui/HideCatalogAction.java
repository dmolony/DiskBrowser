package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
class HideCatalogAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public HideCatalogAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Show catalog panel");

    putValue (Action.SHORT_DESCRIPTION, "Show/hide the catalog panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt C"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_C);
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