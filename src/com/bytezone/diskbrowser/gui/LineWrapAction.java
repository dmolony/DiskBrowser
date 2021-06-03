package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
class LineWrapAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public LineWrapAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Line wrap");

    putValue (Action.SHORT_DESCRIPTION, "Wrap/don't wrap the text in the output panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt W"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_W);
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