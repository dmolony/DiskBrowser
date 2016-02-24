package com.bytezone.diskbrowser.appleworks;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class AppleworksADBFile extends AbstractFile
{
  static final String line = "-------------------------------------------------------"
        + "-----------------------------------\n";

  private final int headerSize;
  private final int cursorDirectionSRL;
  private final char cursorDirectionMRL;
  private final char currentDisplay;
  final int categories;
  private final int totalReports;
  private final int totalRecords;
  private final int dbMinVersion;
  final String[] categoryNames;
  int maxCategoryName;

  private final int[] columnWidthsMRL = new int[30];
  private final int[] columnCategoryMRL = new int[30];
  private final int[] rowPositionSRL = new int[30];
  private final int[] columnPositionSRL = new int[30];
  private final int[] categorySRL = new int[30];

  private final int firstFrozenColumn;
  private final int lastFrozenColumn;
  private final int leftmostActiveColumn;
  private final int totalCategoriesMRL;

  private final int[] selectionRules = new int[3];
  private final int[] testTypes = new int[3];
  private final int[] continuation = new int[3];
  private final String[] comparison = new String[3];

  private final List<Report> reports = new ArrayList<Report> ();
  final List<Record> records = new ArrayList<Record> ();
  private final Record standardRecord;

  public AppleworksADBFile (String name, byte[] buffer)
  {
    super (name, buffer);

    dbMinVersion = buffer[218] & 0xFF;

    headerSize = HexFormatter.getWord (buffer, 0);
    cursorDirectionSRL = buffer[30];
    cursorDirectionMRL = (char) buffer[31];
    currentDisplay = (char) buffer[34];
    categories = buffer[35] & 0xFF;
    categoryNames = new String[categories];

    totalReports = buffer[38] & 0xFF;
    int recs = HexFormatter.getWord (buffer, 36);
    totalRecords = dbMinVersion == 0 ? recs : recs & 0x7FFF;

    for (int i = 0; i < 30; i++)
    {
      columnWidthsMRL[i] = buffer[42 + i] & 0xFF;
      columnCategoryMRL[i] = buffer[78 + i] & 0xFF;
      columnPositionSRL[i] = buffer[114 + i] & 0xFF;
      rowPositionSRL[i] = buffer[150 + i] & 0xFF;
      categorySRL[i] = buffer[186 + i] & 0xFF;
    }

    firstFrozenColumn = buffer[219] & 0xFF;
    lastFrozenColumn = buffer[220] & 0xFF;
    leftmostActiveColumn = buffer[221] & 0xFF;
    totalCategoriesMRL = buffer[222] & 0xFF;

    for (int i = 0; i < 3; i++)
    {
      selectionRules[i] = HexFormatter.getWord (buffer, 223 + i * 2);
      testTypes[i] = HexFormatter.getWord (buffer, 229 + i * 2);
      continuation[i] = HexFormatter.getWord (buffer, 235 + i * 2);
      comparison[i] = new String (buffer, 241 + i * 20, 20);
    }

    int ptr = 357;
    for (int i = 0; i < categoryNames.length; i++)
    {
      categoryNames[i] = new String (buffer, ptr + 1, buffer[ptr] & 0xFF);
      if (categoryNames[i].length () > maxCategoryName)
        maxCategoryName = categoryNames[i].length ();
      ptr += 22;
    }

    for (int reportNo = 0; reportNo < totalReports; reportNo++)
    {
      int reportFormat = (char) buffer[ptr + 214];
      if (reportFormat == 'H')
        reports.add (new TableReport (this, buffer, ptr));
      else if (reportFormat == 'V')
        reports.add (new LabelReport (this, buffer, ptr));
      else
        System.out.println ("Bollocks - report format not H or V : " + reportFormat);
      ptr += 600;
    }

    int length = HexFormatter.getWord (buffer, ptr);
    ptr += 2;

    if (length == 0)
      standardRecord = null;
    else
    {
      standardRecord = new Record (this, buffer, ptr);
      ptr += length;

      for (int recordNo = 0; recordNo < totalRecords; recordNo++)
      {
        length = HexFormatter.getWord (buffer, ptr);
        ptr += 2;
        if (length == 0)
          break;

        records.add (new Record (this, buffer, ptr));
        ptr += length;
      }
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Header size ........ %d%n", headerSize));
    text.append (String
          .format ("SRL cursor ......... %d  (1=default, 2=left->right, top->bottom)%n",
                   cursorDirectionSRL));
    text.append (String.format ("MRL cursor ......... %s  (D=down, R=right)%n",
                                cursorDirectionMRL));
    text.append (String.format ("Display ............ %s  (R=SRL, /=MRL)%n", currentDisplay));
    text.append (String.format ("Categories ......... %d%n", categories));
    text.append (String.format ("Reports ............ %d%n", totalReports));
    text.append (String.format ("Records ............ %d%n", totalRecords));
    text.append (String.format ("Min version ........ %d%n", dbMinVersion));
    text.append (String.format ("1st Frozen col ..... %d%n", firstFrozenColumn));
    text.append (String.format ("Last Frozen col .... %d%n", lastFrozenColumn));
    text.append (String.format ("Left active col .... %d%n", leftmostActiveColumn));
    text.append (String.format ("MRL categories ..... %d%n", totalCategoriesMRL));

    text.append ("\n  Categories:\n");
    for (int i = 0; i < categories; i++)
      text.append (String.format ("  %2d  %-30s %n", (i + 1), categoryNames[i]));
    text.append ("\n");

    for (Report report : reports)
    {
      text.append (report);
      text.append ("\n");
    }

    for (Report report : reports)
    {
      text.append (report.getText ());
      text.append ("\n");
    }

    //    if (reports.size () == 0)
    {
      text.append (line);
      for (Record record : records)
      {
        text.append (record.getReportLine () + "\n");
        text.append (line);
      }
    }

    removeTrailing (text, '\n');
    return text.toString ();
  }

  private void removeTrailing (StringBuilder text, char c)
  {
    while (text.charAt (text.length () - 1) == c)
      text.deleteCharAt (text.length () - 1);
  }
}