package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.bytezone.common.DefaultAction;

// -----------------------------------------------------------------------------------//
class CreateDatabaseAction extends DefaultAction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public CreateDatabaseAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Create Database", "Not working yet", null);
    //		putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt A"));
    //		putValue (Action.MNEMONIC_KEY, KeyEvent.VK_A);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    JOptionPane.showMessageDialog (null, "Coming soon...", "Database",
        JOptionPane.INFORMATION_MESSAGE);
  }
}