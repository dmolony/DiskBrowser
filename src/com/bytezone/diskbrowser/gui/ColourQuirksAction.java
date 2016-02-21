package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class ColourQuirksAction extends AbstractAction
{
  private final DataPanel owner;

  public ColourQuirksAction (DataPanel owner)
  {
    super ("Colour quirks");
    putValue (Action.SHORT_DESCRIPTION, "Display pixels like a TV screen");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt Q"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_Q);
    this.owner = owner;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    owner.setColourQuirks (((JMenuItem) e.getSource ()).isSelected ());
  }
}