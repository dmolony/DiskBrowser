package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;

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

    // block 0 will not read when it is the only DiskAddress in the list
    List<DiskAddress> blocks = event.getSectors ();
    Disk disk = event.getFormattedDisk ().getDisk ();
    byte[] buffer =
        blocks.size () == 1 ? disk.readBlock (blocks.get (0)) : disk.readBlocks (blocks);

    setSelectedFile (new File ("SavedSectors.bin"));

    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
      saveBuffer (buffer);
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
