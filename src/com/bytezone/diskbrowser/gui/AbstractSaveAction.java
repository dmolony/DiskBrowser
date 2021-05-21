package com.bytezone.diskbrowser.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.bytezone.diskbrowser.utilities.DefaultAction;

// -----------------------------------------------------------------------------------//
public abstract class AbstractSaveAction extends DefaultAction
// -----------------------------------------------------------------------------------//
{
  private JFileChooser fileChooser;
  private String dialogTitle;

  // ---------------------------------------------------------------------------------//
  public AbstractSaveAction (String menuText, String tip, String dialogTitle)
  // ---------------------------------------------------------------------------------//
  {
    super (menuText, tip);
    this.dialogTitle = dialogTitle;
  }

  // ---------------------------------------------------------------------------------//
  void setSelectedFile (File file)
  // ---------------------------------------------------------------------------------//
  {
    if (fileChooser == null)
    {
      fileChooser = new JFileChooser ();
      fileChooser.setDialogTitle (dialogTitle);
    }

    fileChooser.setSelectedFile (file);
  }

  // ---------------------------------------------------------------------------------//
  void saveBuffer (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile ();
      try
      {
        Files.write (file.toPath (), buffer, StandardOpenOption.CREATE_NEW);
        JOptionPane.showMessageDialog (null,
            String.format ("File %s saved", file.getName ()));
      }
      catch (FileAlreadyExistsException e)
      {
        JOptionPane.showMessageDialog (null,
            "File " + file.getName () + " already exists", "Failed",
            JOptionPane.ERROR_MESSAGE);
      }
      catch (IOException e)
      {
        e.printStackTrace ();
        JOptionPane.showMessageDialog (null, "File failed to save - " + e.getMessage (),
            "Failed", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  //  void saveFile (Path sourcePath)
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
  //    {
  //      File file = fileChooser.getSelectedFile ();
  //      try
  //      {
  //        Files.copy (sourcePath, file.toPath ());
  //        JOptionPane.showMessageDialog (null,
  //            String.format ("File %s saved", file.getName ()));
  //      }
  //      catch (FileAlreadyExistsException e)
  //      {
  //        JOptionPane.showMessageDialog (null,
  //            "File " + file.getName () + " already exists", "Failed",
  //            JOptionPane.ERROR_MESSAGE);
  //      }
  //      catch (IOException e)
  //      {
  //        e.printStackTrace ();
  //        JOptionPane.showMessageDialog (null, "File failed to save - " + e.getMessage (),
  //            "Failed", JOptionPane.ERROR_MESSAGE);
  //      }
  //    }
  //  }
}
