package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.bytezone.diskbrowser.utilities.DefaultAction;

// this is not being used
// -----------------------------------------------------------------------------------//
class AboutAction extends DefaultAction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  AboutAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("About...", "Display build information", "/com/bytezone/diskbrowser/icons/");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt A"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_A);

    setIcon (Action.SMALL_ICON, "information_16.png");
    setIcon (Action.LARGE_ICON_KEY, "information_32.png");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    about ();
  }

  // ---------------------------------------------------------------------------------//
  public void about ()
  // ---------------------------------------------------------------------------------//
  {
    JOptionPane.showMessageDialog (null, "Author - Denis Molony"       //
        + "\nGitHub - https://github.com/dmolony/DiskBrowser",         //
        //        + "\nContact - dmolony@iinet.net.au",         //
        "About DiskBrowser", JOptionPane.INFORMATION_MESSAGE);
  }
}