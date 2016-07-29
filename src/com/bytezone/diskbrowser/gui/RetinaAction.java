package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

public class RetinaAction extends AbstractAction
{
  private final DiskLayoutPanel owner;

  public RetinaAction (DiskLayoutPanel owner)
  {
    super ("Retina display");
    putValue (Action.SHORT_DESCRIPTION, "use hi resolution graphics");
    //    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt G"));
    //    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_G);
    this.owner = owner;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    owner.setRetina (((JMenuItem) e.getSource ()).isSelected ());
  }
}