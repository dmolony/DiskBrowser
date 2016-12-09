package com.bytezone.diskbrowser.duplicates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class DiskTableModel extends AbstractTableModel
{
  static final String[] headers = { "Disk name", "Actual disk", "Checksum" };

  Map<String, DiskDetails> fileNameMap;
  Map<Long, DiskDetails> checkSumMap;
  List<TableLine> lines = new ArrayList<DiskTableModel.TableLine> ();

  public DiskTableModel (DuplicateHandler duplicateHandler)
  {
    fileNameMap = duplicateHandler.getFileNameMap ();
    checkSumMap = duplicateHandler.getChecksumMap ();

    for (String key : fileNameMap.keySet ())
    {
      DiskDetails original = fileNameMap.get (key);

      if (false)
      {
        if (original.getDuplicateNames ().size () > 0)
        {
          lines.add (new TableLine (original));
          for (DiskDetails duplicate : original.getDuplicateNames ())
            lines.add (new TableLine (duplicate));
        }
      }
      else
      {
        if (original.getDuplicateChecksums ().size () > 0)
        {
          lines.add (new TableLine (original));
          for (DiskDetails duplicate : original.getDuplicateChecksums ())
            lines.add (new TableLine (duplicate));
        }
        //        else if (original.isDuplicateChecksum ())
        //        {
        //          lines.add (new TableLine (key, original));
        //          DiskDetails dd = checkSumMap.get (original.getChecksum ());
        //          for (DiskDetails duplicate : dd.getDuplicateChecksums ())
        //            lines.add (new TableLine (key, duplicate));
        //        }
      }
    }
  }

  @Override
  public String getColumnName (int column)
  {
    return headers[column];
  }

  @Override
  public int getRowCount ()
  {
    return lines.size ();
  }

  @Override
  public int getColumnCount ()
  {
    return headers.length;
  }

  @Override
  public Class<?> getColumnClass (int columnIndex)
  {
    switch (columnIndex)
    {
      case 0:
        return String.class;
      case 1:
        return String.class;
      case 2:
        return Long.class;
      default:
        return Object.class;
    }
  }

  @Override
  public Object getValueAt (int rowIndex, int columnIndex)
  {
    TableLine line = lines.get (rowIndex);
    switch (columnIndex)
    {
      case 0:
        return line.shortName;
      case 1:
        return line.diskDetails.getRootName ();
      case 2:
        return line.checksum;
      default:
        return "???";
    }
  }

  class TableLine
  {
    String shortName;
    DiskDetails diskDetails;
    long checksum;

    public TableLine (DiskDetails diskDetails)
    {
      this.shortName = diskDetails.getShortName ();
      this.diskDetails = diskDetails;
      this.checksum = diskDetails.getChecksum ();
    }
  }
}