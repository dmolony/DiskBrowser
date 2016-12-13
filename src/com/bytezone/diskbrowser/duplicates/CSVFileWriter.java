package com.bytezone.diskbrowser.duplicates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTable;

public class CSVFileWriter
{
  public static void write (DiskTableModel diskTableModel, JTable table)
  {
    String csvFile =
        System.getProperty ("user.home") + File.separator + "DiskBrowser.csv";

    FileWriter writer;

    JFileChooser fileChooser = new JFileChooser ();
    fileChooser.setSelectedFile (new File (csvFile));
    int returnValue = fileChooser.showSaveDialog (null);
    if (returnValue != JFileChooser.APPROVE_OPTION)
      return;

    File selectedFile = fileChooser.getSelectedFile ();

    try
    {
      writer = new FileWriter (selectedFile);
      writer.append (String
          .format ("Path,Name,Type,Size,Duplicate Name, Duplicate Data, Checksum%n"));

      for (int i = 0; i < table.getRowCount (); i++)
      {
        int actualRow = table.convertRowIndexToModel (i);
        String line = diskTableModel.getCSV (actualRow);
        writer.append (line);
      }

      writer.flush ();
      writer.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }
}