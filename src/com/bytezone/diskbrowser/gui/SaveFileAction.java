package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import com.bytezone.diskbrowser.applefile.AppleFileSource;

// -----------------------------------------------------------------------------------//
class SaveFileAction extends AbstractSaveAction implements FileSelectionListener
//-----------------------------------------------------------------------------------//
{
  AppleFileSource appleFileSource;

  // ---------------------------------------------------------------------------------//
  SaveFileAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Save file...", "Save currently selected file", "Save File");
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

    setSelectedFile (new File (appleFileSource.getUniqueName () + ".bin"));
    saveBuffer (appleFileSource.getDataSource ().getBuffer ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void fileSelected (FileSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    this.appleFileSource = event.appleFileSource;
    setEnabled (
        event.appleFileSource != null && event.appleFileSource.getDataSource () != null
            && event.appleFileSource.getDataSource ().getBuffer () != null);
  }
}
