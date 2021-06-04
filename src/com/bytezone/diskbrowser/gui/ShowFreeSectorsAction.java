package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
class ShowFreeSectorsAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  ShowFreeSectorsAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Show free sectors");

    putValue (Action.SHORT_DESCRIPTION,
        "Display which sectors are marked free in the disk layout panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt F"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_F);
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