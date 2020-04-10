package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.bytezone.common.DefaultAction;

// -----------------------------------------------------------------------------------//
class SaveSectorsAction extends DefaultAction implements SectorSelectionListener
// -----------------------------------------------------------------------------------//
{
  SectorSelectedEvent event;

  // ---------------------------------------------------------------------------------//
  SaveSectorsAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Save sectors...", "Save sectors");
    this.setEnabled (false);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent evt)
  // ---------------------------------------------------------------------------------//
  {
    if (event == null)
    {
      System.out.println ("No sectors");
      return;
    }
    byte[] buffer =
        event.getFormattedDisk ().getDisk ().readBlocks (event.getSectors ());

    JFileChooser fileChooser = new JFileChooser ();
    fileChooser.setDialogTitle ("Save sectors");
    fileChooser.setSelectedFile (new File ("saved-" + buffer.length + ".bin"));
    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile ();
      try
      {
        Files.write (file.toPath (), buffer, StandardOpenOption.CREATE_NEW);
        JOptionPane.showMessageDialog (null, "File saved");
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void sectorSelected (SectorSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    this.event = event;
    this.setEnabled (true);
  }
}
