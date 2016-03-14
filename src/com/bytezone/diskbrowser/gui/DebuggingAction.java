package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class DebuggingAction extends AbstractAction
{
  private final DataPanel owner;

  public DebuggingAction (DataPanel owner)
  {
    super ("Debugging");
    putValue (Action.SHORT_DESCRIPTION, "Show debugging information");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt G"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_G);
    this.owner = owner;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    owner.setDebug (((JMenuItem) e.getSource ()).isSelected ());
  }
}