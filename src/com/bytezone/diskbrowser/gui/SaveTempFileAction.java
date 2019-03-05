package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.disk.FormattedDisk;

public class SaveTempFileAction extends DefaultAction
{
  FormattedDisk disk;

  public SaveTempFileAction ()
  {
    super ("Save converted file...", "Save converted file");
  }

  @Override
  public void actionPerformed (ActionEvent evt)
  {
    JFileChooser fileChooser = new JFileChooser ();
    fileChooser.setDialogTitle ("Save converted disk");
    String name = disk.getName ();
    fileChooser.setSelectedFile (new File (name + ".dsk"));
    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile ();
      try
      {
        Files.copy (disk.getDisk ().getFile ().toPath (), file.toPath ());
        JOptionPane.showMessageDialog (null, "Disk saved");
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
    }
  }

  void setDisk (FormattedDisk disk)
  {
    this.disk = disk;
  }
}
