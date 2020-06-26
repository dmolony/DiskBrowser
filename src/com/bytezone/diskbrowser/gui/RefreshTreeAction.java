package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.bytezone.diskbrowser.utilities.DefaultAction;

// -----------------------------------------------------------------------------------//
class RefreshTreeAction extends DefaultAction
// -----------------------------------------------------------------------------------//
{
  CatalogPanel owner;

  // ---------------------------------------------------------------------------------//
  public RefreshTreeAction (CatalogPanel owner)
  // ---------------------------------------------------------------------------------//
  {
    super ("Refresh current tree", "Makes newly added/modified disks available",
        "/com/bytezone/diskbrowser/icons/");
    //    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt R"));
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_F5, 0));
    //    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_R);
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_F5);
    this.owner = owner;

    setIcon (Action.SMALL_ICON, "arrow_refresh.png");
    setIcon (Action.LARGE_ICON_KEY, "arrow_refresh_32.png");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    owner.refreshTree ();
  }
}