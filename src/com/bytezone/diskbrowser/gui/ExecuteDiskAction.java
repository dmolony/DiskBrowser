package com.bytezone.diskbrowser.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

class ExecuteDiskAction extends AbstractAction
{
  // should replace this by making the action a listener
  MenuHandler owner;

  public ExecuteDiskAction (MenuHandler owner)
  {
    super ("Run current disk");
    putValue (Action.SHORT_DESCRIPTION, "Same as double-clicking on the disk");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt X"));
    this.owner = owner;
  }

  public void actionPerformed (ActionEvent e)
  {
    try
    {
      Desktop.getDesktop ().open (owner.currentDisk.getDisk ().getFile ());
    }
    catch (IOException e1)
    {
      e1.printStackTrace ();
      JOptionPane.showMessageDialog (null, "Error opening disk : "
            + owner.currentDisk.getDisk ().getFile (), "Bugger", JOptionPane.INFORMATION_MESSAGE);
    }
  }
}