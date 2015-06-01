package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

class LineWrapAction extends AbstractAction
{
  JTextArea owner;

  public LineWrapAction (JTextArea owner)
  {
    super ("Line wrap");
    putValue (Action.SHORT_DESCRIPTION, "Print the contents of the output panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt W"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_W);
    this.owner = owner;
  }

  public void actionPerformed (ActionEvent e)
  {
    owner.setLineWrap (((JMenuItem) e.getSource ()).isSelected ());
  }
}