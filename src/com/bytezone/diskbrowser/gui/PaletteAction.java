package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

public class PaletteAction extends AbstractAction
{
  private final DataPanel owner;

  public PaletteAction (DataPanel owner)
  {
    super ("Cycle Palette");
    putValue (Action.SHORT_DESCRIPTION, "Select next color palette");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt P"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_P);
    this.owner = owner;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    owner.cyclePalette ();
  }
}