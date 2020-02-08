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

// -----------------------------------------------------------------------------------//
class HideLayoutAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  JFrame owner;
  JPanel layoutPanel;

  // ---------------------------------------------------------------------------------//
  public HideLayoutAction (JFrame owner, JPanel layoutPanel)
  // ---------------------------------------------------------------------------------//
  {
    super ("Show disk layout panel");
    putValue (Action.SHORT_DESCRIPTION, "Show/hide the disk layout panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt D"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_D);
    this.owner = owner;
    this.layoutPanel = layoutPanel;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    set (((JMenuItem) e.getSource ()).isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  public void set (boolean show)
  // ---------------------------------------------------------------------------------//
  {
    if (show)
    {
      owner.add (layoutPanel, BorderLayout.EAST);
      owner.validate ();
    }
    else
    {
      owner.remove (layoutPanel);
      owner.validate ();
    }
  }
}