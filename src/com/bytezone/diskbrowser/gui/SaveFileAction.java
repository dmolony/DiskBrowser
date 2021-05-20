package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.JFileChooser;
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
    super ("Save file...", "Save currently selected file");
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

    if (fileChooser == null)
    {
      fileChooser = new JFileChooser ();
      fileChooser.setDialogTitle ("Save File");
    }

    fileChooser.setSelectedFile (new File (appleFileSource.getUniqueName () + ".bin"));

    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile ();
      try
      {
        Files.write (file.toPath (), appleFileSource.getDataSource ().getBuffer (),
            StandardOpenOption.CREATE_NEW);
        JOptionPane.showMessageDialog (null,
            String.format ("File %s saved", file.getName ()));
      }
      catch (IOException e)
      {
        e.printStackTrace ();
        JOptionPane.showMessageDialog (null, "File failed to save");
      }
    }
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
