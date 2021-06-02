package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
public class ColourQuirksAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
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
    firePropertyChange (e.getActionCommand (), null,
        ((JMenuItem) e.getSource ()).isSelected ());
  }
}