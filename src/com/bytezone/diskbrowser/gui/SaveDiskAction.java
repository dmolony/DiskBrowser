package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.bytezone.diskbrowser.disk.FormattedDisk;

// -----------------------------------------------------------------------------------//
class SaveDiskAction extends AbstractSaveAction implements DiskSelectionListener
// -----------------------------------------------------------------------------------//
{
  FormattedDisk disk;

  // ---------------------------------------------------------------------------------//
  SaveDiskAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Save converted disk...", "Save converted disk");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent evt)
  // ---------------------------------------------------------------------------------//
  {
    if (disk == null)
    {
      JOptionPane.showMessageDialog (null, "No disk selected");
      return;
    }

    if (fileChooser == null)
    {
      fileChooser = new JFileChooser ();
      fileChooser.setDialogTitle ("Save converted disk");
    }

    fileChooser.setSelectedFile (new File (disk.getName () + ".dsk"));

    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile ();
      try
      {
        Files.copy (disk.getDisk ().getFile ().toPath (), file.toPath ());
        JOptionPane.showMessageDialog (null,
            String.format ("File %s saved", file.getName ()));
      }
      catch (IOException e)
      {
        e.printStackTrace ();
        JOptionPane.showMessageDialog (null, "Disk failed to save");
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void diskSelected (DiskSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    this.disk = event.getFormattedDisk ();
    setEnabled (disk != null && disk.isTempDisk ());
  }
}
