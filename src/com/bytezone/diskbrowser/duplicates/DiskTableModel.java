package com.bytezone.diskbrowser.duplicates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.bytezone.diskbrowser.utilities.Utility;

public class DiskTableModel extends AbstractTableModel
{
  static final String[] headers =
      { "Path", "Name", "Type", "Size", "Dup name", "Dup data", "Checksum" };

  Map<String, DiskDetails> fileNameMap;
  Map<Long, DiskDetails> checkSumMap;
  List<TableLine> lines = new ArrayList<DiskTableModel.TableLine> ();

  public DiskTableModel (RootFolderData rootFolderData)
  {
    fileNameMap = rootFolderData.fileNameMap;
    checkSumMap = rootFolderData.checksumMap;

    for (String key : fileNameMap.keySet ())
    {
      DiskDetails original = fileNameMap.get (key);
      lines.add (new TableLine (original));

      for (DiskDetails duplicate : original.getDuplicateNames ())
        lines.add (new TableLine (duplicate));
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
    return lines.isEmpty () ? Object.class : getValueAt (0, columnIndex).getClass ();
  }

  @Override
  public Object getValueAt (int rowIndex, int columnIndex)
  {
    TableLine line = lines.get (rowIndex);
    switch (columnIndex)
    {
      case 0:
        return line.path;
      case 1:
        return line.shortName;
      case 2:
        return line.type;
      case 3:
        return line.size;
      case 4:
        return line.duplicateNames;
      case 5:
        return line.duplicateChecksums;
      case 6:
        return line.checksum;
      default:
        return "???";
    }
  }

  class TableLine
  {
    private final String shortName;
    private final String path;
    private final long checksum;
    private final int duplicateNames;
    private final int duplicateChecksums;
    final DiskDetails diskDetails;
    private final String type;
    private final long size;

    public TableLine (DiskDetails diskDetails)
    {
      this.diskDetails = diskDetails;
      shortName = diskDetails.getShortName ();
      checksum = diskDetails.getChecksum ();
      type = Utility.getSuffix (shortName);
      size = diskDetails.getFile ().length ();

      String rootName = diskDetails.getRootName ();
      path = rootName.substring (0, rootName.length () - shortName.length ());

      if (diskDetails.isDuplicateChecksum ())
      {
        DiskDetails original = checkSumMap.get (diskDetails.getChecksum ());
        duplicateChecksums = original.getDuplicateChecksums ().size () + 1;
      }
      else
        duplicateChecksums = diskDetails.getDuplicateChecksums ().size () + 1;

      if (diskDetails.isDuplicateName ())
      {
        DiskDetails original = fileNameMap.get (diskDetails.getShortName ());
        duplicateNames = original.getDuplicateNames ().size () + 1;
      }
      else
        duplicateNames = diskDetails.getDuplicateNames ().size () + 1;
    }
  }
}