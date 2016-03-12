package com.bytezone.diskbrowser.visicalc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class Sheet implements Iterable<Cell>
{
  private static final Pattern addressPattern =
      Pattern.compile ("([AB]?[A-Z])([0-9]{1,3}):");

  private final Map<Integer, Cell> sheet = new TreeMap<Integer, Cell> ();
  private final List<String> lines = new ArrayList<String> ();

  Cell currentCell = null;
  char defaultFormat;

  private final Map<Integer, Integer> columnWidths = new TreeMap<Integer, Integer> ();
  private int columnWidth = 12;
  private char recalculation = ' ';
  private char recalculationOrder = ' ';
  private int columns;
  private int rows;

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
  
  /D   DELETES THE ROW(/DR) OR COLUMN(/DC) ON WHICH THE CURSOR
       LIES.
  
  /E   ALLOWS EDITING OF THE ENTRY CONTENTS OF ANY ENTRY POSITION
       BY REDISPLAYING IT ON THE EDIT LINE. USE <- -> KEYS & ESC.
  
  /F   SETS THE DISPLAY FORMAT OF AN ENTRY TO ONE OF THE FOLLOWING
       FORMATS:
  
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
  /S   STORAGE COMMANDS ARE AS FOLLOWS:
  
      /SS  SAVE
      /SL  LOAD
      /SD  DELETES SPECIFIED FILE ON DISK
      /SI  INITIALIZE A DISK ON SPECIFIED DRIVE
      /SQ  QUITS VISICALC
  
  /T   SETS A HORIZONTAL TITLE AREA(/TH), A VERTICAL TITLE AREA
       (/TV), SET BOTH A HORIZONTAL & VERTICAL TITLE AREA(/TB)
       OR RESETS THE WINDOWS TO HAVE NO TITLE AREAS(/TN)
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
    while (buffer[--last] == 0)
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

    if (true)
    {
      System.out.println ();
      System.out.println ("Lines:");
      for (String line : lines)
        System.out.println (line);

      System.out.println ();
      System.out.println ("Cells:");
      for (Cell cell : sheet.values ())
        System.out.println (cell);

      System.out.println ();
      System.out.println ("Column widths:");
      System.out.printf ("Default width : %3d%n", columnWidth);
      for (Map.Entry<Integer, Integer> entry : columnWidths.entrySet ())
        System.out.printf ("    column %3d: %3d%n", entry.getKey (), entry.getValue ());
    }
  }

  private int getLineLength (byte[] buffer, int offset)
  {
    int ptr = offset;
    while (buffer[ptr] != (byte) 0x8D)        // end-of-line token
      ptr++;
    return ptr - offset;
  }

  private void processLine (String line)
  {
    // NB no closing bracket: [>K11:@SUM(J11...F11]

    if (line.isEmpty ())
    {
      System.out.println ("empty command");
      return;
    }

    if (line.startsWith ("/"))
    {
      switch (line.charAt (1))
      {
        case 'W':
          System.out.printf ("Skipping [%s]%n", line);
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
              System.out.printf ("Skipping [%s]%n", line);
              break;
            case 'C':
              columnWidth = Integer.parseInt (line.substring (3));
              break;
          }
          break;
        case 'X':
          System.out.printf ("Skipping [%s]%n", line);
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
      currentCell = sheet.get (address.sortValue);

      int pos = line.indexOf (':');                     // end of cell address
      line = line.substring (pos + 1);                  // remove address from line

      if (currentCell == null)
      {
        currentCell = new Cell (this, address);
        if (!line.startsWith ("/G"))
        {
          sheet.put (currentCell.address.sortValue, currentCell);
          if (address.row > rows)
            rows = address.row;
          if (address.column > columns)
            columns = address.column;
        }
      }
    }
    else
    {
      System.out.printf ("Invalid cell address: %s%n", line);
      return;
    }

    assert currentCell != null;

    if (line.startsWith ("/G"))               // global column widths
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
      String fmt = line.substring (0, 3);
      currentCell.format (fmt);               // formatting command
      line = line.substring (3);
      format += fmt;
    }

    if (!line.isEmpty ())
      currentCell.setValue (line);            // expression

    if (true)
      System.out.printf ("[%s][%-3s][%s]%n", currentCell.address, format, line);
  }

  Cell getCell (String addressText)
  {
    Address address = new Address (addressText);
    return getCell (address);
  }

  Cell getCell (Address address)
  {
    Cell cell = sheet.get (address.sortValue);
    if (cell == null)
      System.out.printf ("Nonexistent cell requested [%s]%n", address);

    return cell;
  }

  public int size ()
  {
    return sheet.size ();
  }

  @Override
  public Iterator<Cell> iterator ()
  {
    return sheet.values ().iterator ();
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

  public String getCells ()
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

    DecimalFormat nf = new DecimalFormat ("$#####0.00");
    //    NumberFormat nf = NumberFormat.getCurrencyInstance ();
    int lastRow = -1;
    int lastColumn = 0;

    StringBuilder heading = new StringBuilder ("    ");
    for (int cellNo = 0; cellNo <= columns; cellNo++)
    {
      int width = columnWidth;
      if (columnWidths.containsKey (cellNo))
        width = columnWidths.get (cellNo);

      if (width == 1)
        heading.append ("=");
      else if (width == 2)
        heading.append ("==");
      else
      {
        char letter1 = cellNo < 26 ? ' ' : cellNo < 52 ? 'A' : 'B';
        char letter2 = (char) ((cellNo % 26) + 'A');
        String fmt =
            String.format ("%s%s%%%d.%ds", letter1, letter2, (width - 2), (width - 2));
        heading.append (String.format (fmt, underline));
      }
    }
    text.append (heading);

    for (Cell cell : sheet.values ())
    {
      while (lastRow < cell.address.row)
      {
        ++lastRow;
        lastColumn = 0;
        text.append (String.format ("%n%03d:", lastRow + 1));
      }

      while (lastColumn < cell.address.column)
      {
        int width = columnWidth;
        if (columnWidths.containsKey (lastColumn))
          width = columnWidths.get (lastColumn);
        text.append (longLine.substring (0, width));
        ++lastColumn;
      }

      ++lastColumn;

      int colWidth = columnWidth;
      if (columnWidths.containsKey (cell.address.column))
        colWidth = columnWidths.get (cell.address.column);

      text.append (cell.getText (colWidth, defaultFormat));
    }
    return text.toString ();
  }
}