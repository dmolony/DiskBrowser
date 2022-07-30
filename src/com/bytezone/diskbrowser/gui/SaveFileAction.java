package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.bytezone.diskbrowser.applefile.AppleFileSource;

// -----------------------------------------------------------------------------------//
class SaveFileAction extends AbstractSaveAction implements FileSelectionListener
//-----------------------------------------------------------------------------------//
{
  AppleFileSource appleFileSource;
  private JCheckBox formatted = new JCheckBox ("Formatted");

  // ---------------------------------------------------------------------------------//
  SaveFileAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Save file...", "Save currently selected file", "Save File");

    fileChooser.setAccessory (formatted);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent evt)
  // ---------------------------------------------------------------------------------//
  {
    if (appleFileSource == null)
    {
      JOptionPane.showMessageDialog (null, "No file selected");
      return;
    }

    if (fileChooser.showSaveDialog (null) != JFileChooser.APPROVE_OPTION)
      return;

    if (formatted.isSelected ())
      saveBuffer (appleFileSource.getDataSource ().getText ().getBytes ());
    else
      saveBuffer (appleFileSource.getDataSource ().getBuffer ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void fileSelected (FileSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    this.appleFileSource = event.appleFileSource;
    setSelectedFile (new File (appleFileSource.getUniqueName () + ".bin"));

    setEnabled (appleFileSource != null && appleFileSource.getDataSource () != null
        && appleFileSource.getDataSource ().getBuffer () != null);
  }
}
