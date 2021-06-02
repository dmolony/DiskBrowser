package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
class MonochromeAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
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
    firePropertyChange (e.getActionCommand (), null,
        ((JMenuItem) e.getSource ()).isSelected ());
  }
}