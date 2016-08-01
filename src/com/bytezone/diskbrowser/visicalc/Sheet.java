package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.visicalc.Value.ValueType;

public class Sheet
{
  private static final Pattern addressPattern =
      Pattern.compile ("([AB]?[A-Z])([0-9]{1,3}):");
  private static final byte END_OF_LINE_TOKEN = (byte) 0x8D;
  private static final int FORMAT_LENGTH = 3;

  private final Map<Integer, Cell> rowOrderCells = new TreeMap<Integer, Cell> ();
  private final Map<Integer, Cell> columnOrderCells = new TreeMap<Integer, Cell> ();
  private final List<String> lines = new ArrayList<String> ();

  private Cell currentCell = null;
  private char defaultFormat;

  private final Map<Integer, Integer> columnWidths = new TreeMap<Integer, Integer> ();
  private int columnWidth = 12;

  private char recalculation = 'A';               // auto/manual
  private char recalculationOrder = 'C';          // row/column 

  private int highestColumn;
  private int highestRow;

  // Maximum cell = BK254

  //Commands:
  //  /G<address>     goto
  //  /B              blank the cell
  //  /C              clear and home
  //  A-Z             label (" to force a label)
  //  0-9.+-()*/@#    value (+ to force a value)

  //  /S              Storage (ISLDWR)
  //  /SI             Storage init
  //  /SS             Storage Save
  //  /SL             Storage Load
  //  /SD             Storage Delete
  //  /SW             Storage Write (to cassette)
  //  /SR             Storage Read (from cassette)

  //  /R              Replicate

  //  /G              Global (CORF)
  //  /GF             Global Format (DGILR$*)
  //  /GFI            Global Format Integer
  //  /GF$            Global Format Currency
  //  /GC             Global Column <width>
  //  /GR             Global 
  //  /GRA            Recalculation Auto
  //  /GO             Global 
  //  /GOC            Calculation Order - Columns first

  //  /T              Titles (HVBN)
  //  /TH             fix Horizontal Titles
  //  /TV             fix Vertical Titles
  //  /TB             fix Both Titles
  //  /TN             fix Neither

  //  /W              Window (HV1SU)
  //  /WV             Window Vertical (split on cursor column)
  //  /WH             Window Horizontal (split on cursor row)

  /*
  from: Apple II TextFiles
  http://www.textfiles.com/apple/
    
                   
                      *----------------------*
                       VISICALC COMMAND CHART
                           BY: NIGHT HAWK
                      *----------------------*
  
  /B   SET AN ENTRY TO BLANK
  
  /C   CLEARS THE SHEET, SETTING ALL ENTRIES TO BLANK
  
  /D   DELETE
      /DR THE ROW
      /DC COLUMN ON WHICH THE CURSOR LIES
  
  /E   ALLOWS EDITING OF THE ENTRY CONTENTS OF ANY ENTRY POSITION
       BY REDISPLAYING IT ON THE EDIT LINE. USE <- -> KEYS & ESC.
  
  /F   FORMATS:
       /FG  GENERAL
       /FI  INTEGER
       /F$  DOLLAR AND CENTS
       /FL  LEFT JUSTIFIED
       /FR  RIGHT JUSTIFIED
       /F*  GRAPH
       /FD  DEFAULT
  
  /G   GLOBAL COMMANDS. THESE APPLY TO THE ENTIRE SHEET OR WINDOW.
           /GC  SETS COLUMN WIDTH
           /GF  SETS THE GLOBAL DEFAULT FORMAT
           /GO  SETS THE ORDER OF RECALCULATION TO BE DOWN THE
                COLUMNS OR ACROSS THE ROWS
           /GR  SETS RECALCULATION TO BE AUTOMATIC(/GRA) OR MANUAL(/GRM).
  
  /I   INSERTS A ROW(/IR) OR A COLUMN(/IC)
  /M   MOVES AN ENTIRE ROW OR COLUMN TO A NEW POSITION.
  /P   PRINT COMMAND
  /R   REPLICATE COMMAND
   
  /S   STORAGE COMMANDS
      /SS  SAVE
      /SL  LOAD
      /SD  DELETES SPECIFIED FILE ON DISK
      /SI  INITIALIZE A DISK ON SPECIFIED DRIVE
      /SQ  QUITS VISICALC
  
  /T   
      /TH SETS A HORIZONTAL TITLE AREA
      /TV SETS A VERTICAL TITLE AREA
      /TB SET BOTH A HORIZONTAL & VERTICAL TITLE AREA
      /TN RESETS THE WINDOWS TO HAVE NO TITLE AREAS
      
  /V   DISPLAYS VISICALC'S VERSION NUMBER ON THE PROMPT LINE
  /W   WINDOW CONTROL
      /WH  HORIZONTAL WINDOW
      /WV  VERTICAL WINDOW
      /W1  RETURNS SCREEN TO ONE WINDOW
      /WS  SYNCHRONIZED WINDOWS
      /WU  UNSYNCHRONIZED
  
  /-  REPEATING LABEL
  */

  public Sheet (byte[] buffer)
  {
    int last = buffer.length;
    while (buffer[--last] == 0)     // ignore trailing zeroes
      ;

    int ptr = 0;
    while (ptr < last)
    {
      int length = getLineLength (buffer, ptr);
      String line = HexFormatter.getString (buffer, ptr, length);
      lines.add (line);
      processLine (line);
      ptr += length + 1;            // +1 for end-of-line token
    }

    calculate (recalculationOrder);

    if (false)
      printDebug ();
  }

  private void calculate (char order)
  {
    Map<Integer, Cell> cells = order == 'R' ? rowOrderCells : columnOrderCells;
    for (Cell cell : cells.values ())
      if (cell.is (ValueType.VALUE))
        cell.calculate ();
  }

  private int getLineLength (byte[] buffer, int offset)
  {
    int ptr = offset;
    while (buffer[ptr] != END_OF_LINE_TOKEN)        // end-of-line token
      ptr++;
    return ptr - offset;
  }

  private void processLine (String line)
  {
    assert !line.isEmpty ();
    //    System.out.println (line);

    if (line.startsWith ("/"))
    {
      switch (line.charAt (1))
      {
        case 'W':
          //          System.out.printf ("Skipping [%s]%n", line);
          break;
        case 'G':
          switch (line.charAt (2))
          {
            case 'R':
              recalculation = line.charAt (3);
              break;
            case 'O':
              recalculationOrder = line.charAt (3);
              break;
            case 'P':
              //              System.out.printf ("Skipping [%s]%n", line);
              break;
            case 'C':
              columnWidth = Integer.parseInt (line.substring (3));
              break;
            case 'F':
              defaultFormat = line.charAt (3);
              break;
            default:
              System.out.printf ("Unknown global format [%s]%n", line);
              break;
          }
          break;
        case 'X':
          //          System.out.printf ("Skipping [%s]%n", line);
          break;
        default:
          System.out.printf ("Skipping [%s]%n", line);
      }
      return;
    }

    if (!line.startsWith (">"))                               // GOTO cell
    {
      System.out.printf ("Error [%s]%n", line);
      return;
    }

    currentCell = null;

    Matcher m = addressPattern.matcher (line);
    if (m.find ())
    {
      Address address = new Address (m.group (1), m.group (2));
      currentCell = rowOrderCells.get (address.rowKey);

      int pos = line.indexOf (':');                     // end of cell address
      line = line.substring (pos + 1);                  // remove address from line

      if (currentCell == null)
      {
        currentCell = new Cell (this, address);
        if (!line.startsWith ("/G"))
          addCell (currentCell);
      }
    }
    else
    {
      System.out.printf ("Invalid cell address: %s%n", line);
      return;
    }

    assert currentCell != null;

    if (line.startsWith ("/G"))                         // global column widths
    {
      if (line.charAt (2) == 'C' && line.charAt (3) == 'C')
      {
        int width = Integer.parseInt (line.substring (4));
        columnWidths.put (currentCell.address.column, width);
      }
      else
        System.out.printf ("Unknown Global:[%s]%n", line);

      return;
    }

    // check for formatting commands
    String format = "";
    while (line.startsWith ("/"))
    {
      String fmt = line.substring (0, FORMAT_LENGTH);
      line = line.substring (FORMAT_LENGTH);

      if (fmt.equals ("/TH") || fmt.equals ("/TV"))     // lock titles ??
      {
        // ignore
      }
      else
        currentCell.format (fmt);                       // formatting command

      format += fmt;
    }

    if (!line.isEmpty ())
      currentCell.setValue (line);                      // expression

    if (false)
      System.out.printf ("[%s][%-3s][%s]%n", currentCell.address, format, line);
  }

  private void addCell (Cell cell)
  {
    rowOrderCells.put (cell.address.rowKey, cell);
    columnOrderCells.put (cell.address.columnKey, cell);

    if (cell.address.row > highestRow)
      highestRow = cell.address.row;
    if (cell.address.column > highestColumn)
      highestColumn = cell.address.column;
  }

  Cell getCell (String addressText)
  {
    return getCell (new Address (addressText));
  }

  Cell getCell (Address address)
  {
    return rowOrderCells.get (address.rowKey);
  }

  public int size ()
  {
    return rowOrderCells.size ();
  }

  public String getLines ()
  {
    StringBuilder text = new StringBuilder ();

    for (String line : lines)
    {
      text.append (line);
      text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  public String getTextDisplay (boolean debug)
  {
    StringBuilder text = new StringBuilder ();
    String longLine;
    if (false)
      longLine = "%+++++++++++++++++++++++++++++++++++++++++++++++++++++++"
          + "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
    else
      longLine = "                                                          "
          + "                                                                      ";
    String underline = "---------------------------------------------------------"
        + "-----------------------------------------------------------------";

    int lastRow = 0;
    int lastColumn = 0;

    StringBuilder heading = new StringBuilder ("    ");
    for (int column = 0; column <= highestColumn; column++)
    {
      int width = columnWidth;
      if (columnWidths.containsKey (column))
        width = columnWidths.get (column);

      char letter1 = column < 26 ? ' ' : column < 52 ? 'A' : 'B';
      char letter2 = (char) ((column % 26) + 'A');

      String fmt =
          String.format ("%s%s%%%d.%ds", letter1, letter2, (width - 2), (width - 2));
      if (width == 1)
        heading.append (letter2);
      else if (width == 2)
        heading.append (String.format ("%s%s", letter1, letter2));
      else
        heading.append (String.format (fmt, underline));
    }

    if (debug)
    {
      text.append (heading);
      text.append ("\n001:");
    }

    for (Cell cell : rowOrderCells.values ())
    {
      Address cellAddress = cell.address;
      while (lastRow < cellAddress.row)
      {
        ++lastRow;
        lastColumn = 0;
        if (debug)
          text.append (String.format ("%n%03d:", lastRow + 1));
        else
          text.append ("\n");
      }

      while (lastColumn < cellAddress.column)
      {
        int width = columnWidth;
        if (columnWidths.containsKey (lastColumn))
          width = columnWidths.get (lastColumn);
        text.append (longLine.substring (0, width));
        ++lastColumn;
      }

      ++lastColumn;

      int colWidth = columnWidth;
      if (columnWidths.containsKey (cellAddress.column))
        colWidth = columnWidths.get (cellAddress.column);

      text.append (cell.getText (colWidth, defaultFormat));
    }
    return text.toString ();
  }

  private void printDebug ()
  {
    System.out.println ();
    System.out.println ("Lines:");
    for (String line : lines)
      System.out.println (line);

    System.out.println ();
    System.out.println ("Cells:");
    for (Cell cell : rowOrderCells.values ())
      System.out.println (cell);

    System.out.println ();
    System.out.println ("Column widths:");
    System.out.printf ("Default width : %3d%n", columnWidth);
    for (Map.Entry<Integer, Integer> entry : columnWidths.entrySet ())
      System.out.printf ("    column %3d: %3d%n", entry.getKey (), entry.getValue ());
  }
}