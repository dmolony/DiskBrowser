package com.bytezone.diskbrowser.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

public class CloseTabAction extends AbstractAction
{
  CatalogPanel catalogPanel;

  public CloseTabAction (CatalogPanel catalogPanel)
  {
    super ("Close Tab");
    putValue (Action.SHORT_DESCRIPTION, "Close the current disk tab");
    //    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("ctrl W"));
    int mask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMaskEx ();
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_W, mask));
    //    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_W);
    this.catalogPanel = catalogPanel;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    catalogPanel.closeCurrentTab ();
  }
}