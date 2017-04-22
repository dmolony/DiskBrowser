package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

class ShowFreeSectorsAction extends AbstractAction
{
  DiskLayoutPanel panel;
  MenuHandler mh;

  public ShowFreeSectorsAction (MenuHandler mh, DiskLayoutPanel panel)
  {
    super ("Show free sectors");
    putValue (Action.SHORT_DESCRIPTION,
        "Display which sectors are marked free in the disk layout panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt F"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_F);
    this.panel = panel;
    this.mh = mh;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    panel.setFree (mh.showFreeSectorsItem.isSelected ());
  }
}