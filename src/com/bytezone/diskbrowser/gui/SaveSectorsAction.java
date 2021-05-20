package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

// -----------------------------------------------------------------------------------//
class SaveSectorsAction extends AbstractSaveAction implements SectorSelectionListener
// -----------------------------------------------------------------------------------//
{
  SectorSelectedEvent event;

  // ---------------------------------------------------------------------------------//
  SaveSectorsAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Save sectors...", "Save currently selected sectors", "Save sectors");
    this.setEnabled (false);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent evt)
  // ---------------------------------------------------------------------------------//
  {
    if (event == null)
    {
      JOptionPane.showMessageDialog (null, "No sectors selected");
      return;
    }

    setSelectedFile (new File ("savedSectors.bin"));
    saveBuffer (event.getFormattedDisk ().getDisk ().readBlocks (event.getSectors ()));

    //    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
    //    {
    //      File file = fileChooser.getSelectedFile ();
    //      try
    //      {
    //        byte[] buffer =
    //            event.getFormattedDisk ().getDisk ().readBlocks (event.getSectors ());
    //        Files.write (file.toPath (), buffer, StandardOpenOption.CREATE_NEW);
    //        JOptionPane.showMessageDialog (null,
    //            String.format ("File %s saved", file.getName ()));
    //      }
    //      catch (IOException e)
    //      {
    //        e.printStackTrace ();
    //        JOptionPane.showMessageDialog (null, "File failed to save");
    //      }
    //  }
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
