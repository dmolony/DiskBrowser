package com.bytezone.diskbrowser.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

public class ScaleAction extends AbstractAction
{
  private final DataPanel owner;
  private double scale;

  // ---------------------------------------------------------------------------------//
  public ScaleAction (DataPanel owner, double scale, int menu)
  // ---------------------------------------------------------------------------------//
  {
    super ("Scale " + scale);

    int mask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMaskEx ();

    putValue (Action.SHORT_DESCRIPTION, "Scale image");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (
        (menu == 1 ? KeyEvent.VK_1 : menu == 2 ? KeyEvent.VK_2 : KeyEvent.VK_3), mask));

    this.owner = owner;
    this.scale = scale;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    owner.setScale (scale);
  }
}
