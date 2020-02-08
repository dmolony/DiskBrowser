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
  private final DataPanel owner;

  // ---------------------------------------------------------------------------------//
  MonochromeAction (DataPanel owner)
  // ---------------------------------------------------------------------------------//
  {
    super ("Monochrome");
    putValue (Action.SHORT_DESCRIPTION, "Display image in monochrome or color");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt M"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_M);
    this.owner = owner;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    owner.setMonochrome (((JMenuItem) e.getSource ()).isSelected ());
  }
}