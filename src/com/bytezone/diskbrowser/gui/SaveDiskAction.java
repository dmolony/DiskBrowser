package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.FormattedDisk;

// -----------------------------------------------------------------------------------//
class SaveDiskAction extends AbstractSaveAction implements DiskSelectionListener
// -----------------------------------------------------------------------------------//
{
  FormattedDisk formattedDisk;

  // ---------------------------------------------------------------------------------//
  SaveDiskAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Save converted disk...", "Save converted disk", "Save converted disk");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent evt)
  // ---------------------------------------------------------------------------------//
  {
    if (formattedDisk == null)
    {
      JOptionPane.showMessageDialog (null, "No disk selected");
      return;
    }

    Disk disk = formattedDisk.getDisk ();
    if (disk instanceof AppleDisk appleDisk)
    {
      int blocks = disk.getTotalBlocks ();
      String suffix = blocks <= 560 ? ".dsk" : ".hdv";

      setSelectedFile (new File (formattedDisk.getName () + suffix));
      //      saveFile (disk.getFile ().toPath ());
      saveBuffer (appleDisk.getBuffer ());
    }
    else
      System.out.println ("Not an AppleDisk");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void diskSelected (DiskSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    formattedDisk = event.getFormattedDisk ();
    setEnabled (formattedDisk != null && formattedDisk.isTempDisk ());
  }
}
