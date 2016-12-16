package com.bytezone.diskbrowser.duplicates;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.bytezone.diskbrowser.utilities.Utility;

public class DiskTableModel extends AbstractTableModel
{
  static final String[] headers =
      { "Path", "Name", "Type", "Size", "# names", "Checksum", "# checksums" };

  private final List<TableLine> lines = new ArrayList<DiskTableModel.TableLine> ();
  private final RootFolderData rootFolderData;

  public DiskTableModel (RootFolderData rootFolderData)
  {
    this.rootFolderData = rootFolderData;

    for (String key : rootFolderData.fileNameMap.keySet ())
    {
      DiskDetails original = rootFolderData.fileNameMap.get (key);
      lines.add (new TableLine (original, rootFolderData));

      for (DiskDetails duplicate : original.getDuplicateNames ())
        lines.add (new TableLine (duplicate, rootFolderData));
    }
  }

  public DiskDetails getDiskDetails (int rowIndex)
  {
    return lines.get (rowIndex).diskDetails;
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
    if (rootFolderData.doChecksums)
      return headers.length;
    else
      return headers.length - 1;
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
        return line.fileName;
      case 2:
        return line.type;
      case 3:
        return line.size;
      case 4:
        return line.duplicateNames;
      case 5:
        return line.checksum;
      case 6:
        return line.duplicateChecksums;
      default:
        return "???";
    }
  }

  public String getCSV (int rowIndex)
  {
    TableLine line = lines.get (rowIndex);
    return String.format ("\"%s\",\"%s\",%s,%d,%s,%s,%d%n", line.path, line.shortName,
        line.type, line.size, line.duplicateNames, line.duplicateChecksums,
        line.checksum);
  }

  void updateChecksum (int rowIndex)
  {
    TableLine line = lines.get (rowIndex);
    line.checksum = line.diskDetails.calculateChecksum ();
    fireTableCellUpdated (rowIndex, 5);
  }

  class TableLine
  {
    private final String shortName;
    private final String fileName;
    private final String path;
    private long checksum;
    private final int duplicateNames;
    private final int duplicateChecksums;
    final DiskDetails diskDetails;
    private final String type;
    private final long size;

    public TableLine (DiskDetails diskDetails, RootFolderData rootFolderData)
    {
      this.diskDetails = diskDetails;
      shortName = diskDetails.getShortName ();
      fileName = diskDetails.getFileName ();
      checksum = diskDetails.getChecksum ();
      type = Utility.getSuffix (shortName);
      size = diskDetails.getFile ().length ();

      String rootName = diskDetails.getRootName ();
      path = rootName.substring (0, rootName.length () - shortName.length ());

      if (rootFolderData.doChecksums)
        if (diskDetails.isDuplicateChecksum ())
        {
          DiskDetails original =
              rootFolderData.checksumMap.get (diskDetails.getChecksum ());
          duplicateChecksums = original.getDuplicateChecksums ().size () + 1;
        }
        else
          duplicateChecksums = diskDetails.getDuplicateChecksums ().size () + 1;
      else
        duplicateChecksums = 0;

      if (diskDetails.isDuplicateName ())
      {
        DiskDetails original =
            rootFolderData.fileNameMap.get (diskDetails.getShortName ());
        duplicateNames = original.getDuplicateNames ().size () + 1;
      }
      else
        duplicateNames = diskDetails.getDuplicateNames ().size () + 1;
    }
  }
}