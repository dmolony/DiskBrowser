package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

class LineWrapAction extends AbstractAction
{
  List<JTextArea> listeners = new ArrayList<> ();

  public LineWrapAction ()
  {
    super ("Line wrap");
    putValue (Action.SHORT_DESCRIPTION, "Print the contents of the output panel");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt W"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_W);
  }

  public void addListener (JTextArea listener)
  {
    if (!listeners.contains (listener))
      listeners.add (listener);
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    for (JTextArea listener : listeners)
      listener.setLineWrap (((JMenuItem) e.getSource ()).isSelected ());
  }
}