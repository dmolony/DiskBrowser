package com.bytezone.diskbrowser.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

class HideCatalogAction extends AbstractAction
{
  JFrame owner;
  JPanel catalogPanel;

  public HideCatalogAction (JFrame owner, JPanel catalogPanel)
  {
    super ("Show catalog panel");
    putValue (Action.SHORT_DESCRIPTION, "Show/hide the catalog panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt C"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_C);
    this.owner = owner;
    this.catalogPanel = catalogPanel;
  }

  public void actionPerformed (ActionEvent e)
  {
    set (((JMenuItem) e.getSource ()).isSelected ());
  }

  public void set (boolean show)
  {
    if (show)
    {
      owner.add (catalogPanel, BorderLayout.WEST);
      owner.validate ();
    }
    else
    {
      owner.remove (catalogPanel);
      owner.validate ();
    }
  }
}