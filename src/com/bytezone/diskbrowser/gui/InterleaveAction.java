package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.disk.FormattedDisk;

// -----------------------------------------------------------------------------------//
class InterleaveAction extends DefaultAction
// -----------------------------------------------------------------------------------//
{
  int interleave;
  FormattedDisk currentDisk;
  static String[] names = { "No Interleave", "Prodos/Pascal", "Infocom", "CPM" };

  // ---------------------------------------------------------------------------------//
  InterleaveAction (int interleave)
  // ---------------------------------------------------------------------------------//
  {
    super (names[interleave], "Alter interleave");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt " + interleave));
    this.interleave = interleave;
  }

  // ---------------------------------------------------------------------------------//
  void setDisk (FormattedDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    currentDisk = disk;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    currentDisk.getDisk ().setInterleave (interleave);
  }
}