package com.bytezone.diskbrowser.duplicates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;

class CheckBoxActionListener implements ActionListener
{
  DiskDetails diskDetails;
  List<DiskDetails> disksSelected;
  JButton deleteButton;
  JButton clearButton;

  public CheckBoxActionListener (DiskDetails diskDetails, List<DiskDetails> disksSelected,
      JButton deleteButton, JButton clearButton)
  {
    this.diskDetails = diskDetails;
    this.disksSelected = disksSelected;
    this.deleteButton = deleteButton;
    this.clearButton = clearButton;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    if (((JCheckBox) e.getSource ()).isSelected ())
      disksSelected.add (diskDetails);
    else
      disksSelected.remove (diskDetails);
    deleteButton.setEnabled (disksSelected.size () > 0);
    clearButton.setEnabled (disksSelected.size () > 0);
  }
}