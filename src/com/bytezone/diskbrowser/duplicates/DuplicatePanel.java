package com.bytezone.diskbrowser.duplicates;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import com.bytezone.input.SpringUtilities;

public class DuplicatePanel extends JPanel
{
  List<JCheckBox> checkBoxes = new ArrayList<JCheckBox> ();
  List<DiskDetails> duplicateDisks;

  public DuplicatePanel (List<DiskDetails> duplicateDisks, int folderNameLength,
      List<DiskDetails> disksSelected, JButton deleteButton, JButton clearButton)
  {
    this.duplicateDisks = duplicateDisks;
    setLayout (new SpringLayout ());
    setAlignmentX (LEFT_ALIGNMENT);

    int count = 0;
    for (DiskDetails dd : duplicateDisks)
    {
      JCheckBox cb = new JCheckBox ();
      checkBoxes.add (cb);

      cb.addActionListener (
          new CheckBoxActionListener (dd, disksSelected, deleteButton, clearButton));
      add (cb);
      if (++count == 1)
        add (new JLabel ("Source disk"));
      else
      {
        String text = dd.isDuplicate () ? "Duplicate" : "OK";
        add (new JLabel (text));
      }
      String checksum = dd.isDuplicate () || count == 1 ? ""
          : " (checksum = " + dd.getChecksum () + ")";
      add (new JLabel (dd.getAbsolutePath ().substring (folderNameLength) + checksum));
    }
    SpringUtilities.makeCompactGrid (this, duplicateDisks.size (), 3, //rows, cols
        10, 0, //initX, initY
        10, 0); //xPad, yPad
  }
}