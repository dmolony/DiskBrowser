package com.bytezone.diskbrowser.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.bytezone.diskbrowser.utilities.DefaultAction;

// -----------------------------------------------------------------------------------//
public abstract class AbstractSaveAction extends DefaultAction
// -----------------------------------------------------------------------------------//
{
  JFileChooser fileChooser;
  String title;

  // ---------------------------------------------------------------------------------//
  public AbstractSaveAction (String text, String tip, String title)
  // ---------------------------------------------------------------------------------//
  {
    super (text, tip);
    this.title = title;
  }

  // ---------------------------------------------------------------------------------//
  void setSelectedFile (File file)
  // ---------------------------------------------------------------------------------//
  {
    if (fileChooser == null)
    {
      fileChooser = new JFileChooser ();
      fileChooser.setDialogTitle (title);
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
      catch (IOException e)
      {
        e.printStackTrace ();
        JOptionPane.showMessageDialog (null, "File failed to save");
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  void saveFile (Path sourcePath)
  // ---------------------------------------------------------------------------------//
  {
    if (fileChooser.showSaveDialog (null) == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile ();
      try
      {
        Files.copy (sourcePath, file.toPath ());
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
}
