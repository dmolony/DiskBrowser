package com.bytezone.diskbrowser.visicalc;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class VisicalcSpreadsheet implements Iterable<VisicalcCell>
{
  private static final Pattern addressPattern =
      Pattern.compile ("([A-B]?[A-Z])([0-9]{1,3}):");
  private static final Pattern functionPattern = Pattern
      .compile ("\\(([A-B]?[A-Z])([0-9]{1,3})\\.\\.\\.([A-B]?[A-Z])([0-9]{1,3})\\)?");
  private static final Pattern addressList = Pattern.compile ("\\(([^,]+(,[^,]+)*)\\)");

  private final Map<Integer, VisicalcCell> sheet = new TreeMap<Integer, VisicalcCell> ();
  private final Map<String, Double> functions = new HashMap<String, Double> ();

  final List<String> lines = new ArrayList<String> ();
  VisicalcCell currentCell = null;
  char defaultFormat;

  private final Map<Integer, Integer> columnWidths = new TreeMap<Integer, Integer> ();
  int columnWidth = 12;

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
  //  /GRA
  //  /GO             Global 
  //  /GOC

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

  public VisicalcSpreadsheet (byte[] buffer)
  {
    int last = buffer.length;
    while (buffer[--last] == 0)
      ;

    int ptr = 0;
    while (ptr <= last)
    {
      int endPtr = findEndPtr (buffer, ptr);
      add (HexFormatter.getString (buffer, ptr, endPtr - ptr));
      ptr = endPtr + 1;
    }

    if (true)
    {
      System.out.println ();
      System.out.println ("Lines:");
      for (String line : lines)
        System.out.println (line);

      System.out.println ();
      System.out.println ("Cells:");
      for (VisicalcCell cell : sheet.values ())
        System.out.println (cell);

      System.out.println ();
      System.out.println ("Column widths:");
      System.out.printf ("Default width : %3d%n", columnWidth);
      for (Map.Entry<Integer, Integer> entry : columnWidths.entrySet ())
        System.out.printf ("    column %3d: %3d%n", entry.getKey (), entry.getValue ());
    }
  }

  private int findEndPtr (byte[] buffer, int ptr)
  {
    while (buffer[ptr] != (byte) 0x8D)
      ptr++;
    return ptr;
  }

  private void add (String command)
  {
    // NB no closing bracket: [>K11:@SUM(J11...F11]

    if (command.isEmpty ())
    {
      System.out.println ("empty command");
      return;
    }

    lines.add (command);

    if (command.startsWith (">"))                               // GOTO cell
    {
      Matcher m = addressPattern.matcher (command);
      if (m.find ())
      {
        Address address = new Address (m.group (1), m.group (2));
        currentCell = sheet.get (address.sortValue);
        int pos = command.indexOf (':');                        // end of cell address
        command = command.substring (pos + 1);

        if (currentCell == null)
        {
          currentCell = new VisicalcCell (this, address);
          if (!command.startsWith ("/GCC"))
            sheet.put (currentCell.address.sortValue, currentCell);
        }
      }
      else
        System.out.printf ("Invalid cell address: %s%n", command);
    }

    if (command.startsWith ("/"))                               // command
    {
      String data = command.substring (1);
      char subCommand = command.charAt (1);
      switch (subCommand)
      {
        case 'W':           // Window control
          //          System.out.println ("  Window command: " + data);
          break;

        case 'G':           // Global command
          //          System.out.println ("  Global command: " + data);
          try
          {
            if (data.charAt (1) == 'C')
            {
              if (data.charAt (2) == 'C')
              {
                int width = Integer.parseInt (data.substring (3));
                int column = currentCell.address.column;
                columnWidths.put (column, width);
              }
              else
                columnWidth = Integer.parseInt (data.substring (2));
            }
            else if (data.charAt (1) == 'F')
              defaultFormat = data.charAt (2);
          }
          catch (NumberFormatException e)
          {
            System.out.printf ("NFE: %s%n", data.substring (2));
          }
          break;

        case 'T':             // Set title area
          //          System.out.println ("  Title command: " + data);
          break;

        case 'X':             // Position cursor?
          break;

        default:
          currentCell.doCommand (command);
      }
    }
    else if (command.startsWith ("@"))
    {
      currentCell.doCommand (command);
    }
    else if (command.startsWith ("\""))
    {
      currentCell.doCommand (command);
    }
    else if (command.startsWith ("+"))
    {
      currentCell.doCommand (command);
    }
    else if (command.matches ("^[0-9.]+$"))         // value
    {
      currentCell.doCommand (command);
    }
    else if (command.matches ("^[-A-Z]+$"))         // label
    {
      currentCell.doCommand (command);
    }
    else
      currentCell.doCommand (command);              // formula
  }

  double evaluateFunction (String function)
  {
    if (functions.containsKey (function))
      return functions.get (function);

    //    System.out.println (function);
    double result = 0;

    if (function.startsWith ("@IF("))
    {
      return result;
    }
    if (function.startsWith ("@LOOKUP("))
    {
      return result;
    }

    Range range = getRange (function);
    if (range == null)
      return result;

    if (function.startsWith ("@SUM"))
    {
      for (Address address : range)
        result += getValue (address);
    }
    else if (function.startsWith ("@COUNT"))
    {
      int count = 0;
      for (Address address : range)
      {
        VisicalcCell cell = getCell (address);
        if (cell != null && cell.hasValue () && cell.getValue () != 0.0)
          ++count;
      }
      result = count;
    }
    else if (function.startsWith ("@MIN"))
    {
      double min = Double.MAX_VALUE;
      for (Address address : range)
        if (min > getValue (address))
          min = getValue (address);
      result = min;
    }
    else if (function.startsWith ("@MAX"))
    {
      double max = Double.MIN_VALUE;
      for (Address address : range)
        if (max < getValue (address))
          max = getValue (address);
      result = max;
    }
    else if (function.startsWith ("@LOOKUP"))
    {
      System.out.println ("Unfinished: " + function);
      result = 0;
    }
    else
      System.out.println ("Unimplemented function: " + function);
    // http://www.bricklin.com/history/refcard1.htm
    // Functions:
    //   @AVERAGE
    //   @NPV
    //   @LOOKUP(v,range)
    //   @NA
    //   @ERROR
    //   @PI
    //   @ABS
    //   @INT
    //   @EXP
    //   @SQRT
    //   @LN
    //   @LOG10
    //   @SIN
    //   @ASIN
    //   @COS
    //   @ACOS
    //   @TAN
    //   @ATAN

    // Unimplemented functions found so far:
    //  @IF
    //  @ISERROR
    //  @OR
    //  @AND

    functions.put (function, result);
    return result;
  }

  Range getRange (String text)
  {
    Range range = null;
    Matcher m = functionPattern.matcher (text);
    while (m.find ())
    {
      Address fromAddress = new Address (m.group (1), m.group (2));
      Address toAddress = new Address (m.group (3), m.group (4));
      range = new Range (fromAddress, toAddress);
    }

    if (range != null)
      return range;

    m = addressList.matcher (text);
    while (m.find ())
    {
      String[] cells = m.group (1).split (",");
      range = new Range (cells);
    }

    if (range != null)
      return range;

    int pos = text.indexOf ("...");
    if (pos > 0)
    {
      String from = text.substring (0, pos);
      String to = text.substring (pos + 3);
      Address fromAddress = new Address (from);
      Address toAddress = new Address (to);
      range = new Range (fromAddress, toAddress);
    }

    if (range != null)
      return range;
    System.out.println ("null range : " + text);

    return range;
  }

  public double getValue (Address address)
  {
    VisicalcCell cell = sheet.get (address.sortValue);
    return cell == null ? 0.0 : cell.getValue ();
  }

  public double getValue (String cellName)
  {
    Address address = new Address (cellName);
    return getValue (address);
  }

  public VisicalcCell getCell (Address address)
  {
    return sheet.get (address.sortValue);
  }

  public int size ()
  {
    return sheet.size ();
  }

  @Override
  public Iterator<VisicalcCell> iterator ()
  {
    return sheet.values ().iterator ();
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

    DecimalFormat nf = new DecimalFormat ("$#####0.00");
    //    NumberFormat nf = NumberFormat.getCurrencyInstance ();
    int lastRow = 0;
    int lastColumn = 0;

    for (VisicalcCell cell : sheet.values ())
    {
      while (lastRow < cell.address.row)
      {
        text.append ("\n");
        ++lastRow;
        lastColumn = 0;
      }

      while (lastColumn < cell.address.column)
      {
        int width = columnWidth;
        if (columnWidths.containsKey (lastColumn))
          width = columnWidths.get (lastColumn);
        text.append (longLine.substring (0, width));
        ++lastColumn;
      }

      int colWidth = columnWidth;
      if (columnWidths.containsKey (cell.address.column))
        colWidth = columnWidths.get (cell.address.column);
      ++lastColumn;

      char format = cell.getFormat ();
      if (format == ' ')
        format = defaultFormat;

      if (cell.hasValue ())
      {
        if (format == 'I')
        {
          String integerFormat = String.format ("%%%d.0f", colWidth);
          //          System.out.printf ("Integer format:%s%n", integerFormat);
          text.append (String.format (integerFormat, cell.getValue ()));
        }
        else if (format == '$')
        {
          String currencyFormat = String.format ("%%%d.%ds", colWidth, colWidth);
          //          System.out.printf ("Currency format:%s%n", currencyFormat);
          text.append (String.format (currencyFormat, nf.format (cell.getValue ())));
        }
        else if (format == '*')
        {
          String graphFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
          text.append (String.format (graphFormat, "********************"));
        }
        else
        {
          // this could be improved
          String numberFormat = String.format ("%%%d.3f", colWidth + 4);
          //          System.out.printf ("Number format:%s%n", numberFormat);
          String val = String.format (numberFormat, cell.getValue ());
          while (val.endsWith ("0"))
            val = ' ' + val.substring (0, val.length () - 1);
          if (val.endsWith ("."))
            val = ' ' + val.substring (0, val.length () - 1);
          if (val.length () > colWidth)
            val = val.substring (val.length () - colWidth);
          text.append (val);
        }
      }
      else
      {
        if (format == 'R')
        {
          String labelFormat = String.format ("%%%d.%ds", colWidth, colWidth);
          //          System.out.printf ("Label format:%s%n", labelFormat);
          text.append (String.format (labelFormat, cell.value ()));
        }
        else
        {
          String labelFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
          //          System.out.printf ("Label format:%s%n", labelFormat);
          text.append (String.format (labelFormat, cell.value ()));
        }
      }
    }
    return text.toString ();
  }
}