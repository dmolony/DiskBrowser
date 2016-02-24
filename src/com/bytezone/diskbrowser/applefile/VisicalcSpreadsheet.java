package com.bytezone.diskbrowser.applefile;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.diskbrowser.applefile.VisicalcSpreadsheet.VisicalcCell;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class VisicalcSpreadsheet implements Iterable<VisicalcCell>
{
  private static final Pattern addressPattern =
      Pattern.compile ("([A-B]?[A-Z])([0-9]{1,3}):");
  private static final Pattern cellContents =
      Pattern.compile ("([-+/*]?)(([A-Z]{1,2}[0-9]{1,3})|([0-9.]+)|(@[^-+/*]+))");
  private static final Pattern functionPattern = Pattern
      .compile ("\\(([A-B]?[A-Z])([0-9]{1,3})\\.\\.\\.([A-B]?[A-Z])([0-9]{1,3})\\)");

  private final Map<Integer, VisicalcCell> sheet = new TreeMap<Integer, VisicalcCell> ();
  private final Map<String, Double> functions = new HashMap<String, Double> ();

  final List<String> lines = new ArrayList<String> ();
  private final Map<Integer, Integer> columnWidths = new TreeMap<Integer, Integer> ();
  VisicalcCell currentCell = null;
  int columnWidth = 12;
  char defaultFormat;

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
  //  /GO             Global 

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
           /F* GRAPH
           /FD DEFAULT
  
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
  
      /SS SAVE
      /SL LOAD
      /SD DELETES SPECIFIED FILE ON DISK
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
    int ptr = 0;
    int last = buffer.length - 1;

    while (buffer[last] == 0)
      last--;

    while (ptr <= last)
    {
      int endPtr = findEndPtr (buffer, ptr);
      add (HexFormatter.getString (buffer, ptr, endPtr - ptr));
      ptr = endPtr + 1;
    }

    if (false)
      for (VisicalcCell cell : sheet.values ())
        System.out.println (cell);

    for (Map.Entry<Integer, Integer> entry : columnWidths.entrySet ())
      System.out.printf ("Width of column %3d: %d%n", entry.getKey (), entry.getValue ());
  }

  public void add (String command)
  {
    lines.add (command);
    String data;

    if (command.startsWith (">"))             // GOTO cell
    {
      int pos = command.indexOf (':');        // end of cell address
      Matcher m = addressPattern.matcher (command);
      if (m.find ())
      {
        Address address = new Address (m.group (1), m.group (2));
        VisicalcCell cell = sheet.get (address.sortValue);
        command = command.substring (pos + 1);

        if (cell == null)
        {
          cell = new VisicalcCell (this, address);
          sheet.put (cell.address.sortValue, cell);
          currentCell = cell;
        }
        else
          System.out.println ("Found " + cell);
      }
      else
        System.out.printf ("Invalid cell address: %s%n", command);
    }

    if (command.startsWith ("/"))        // command
    {
      //      System.out.printf ("Cmd: %s%n", command);
      data = command.substring (1);
      char subCommand = command.charAt (1);
      switch (subCommand)
      {
        case 'W':
          System.out.println ("  Window command: " + data);
          break;

        case 'G':
          System.out.println ("  Global command: " + data);
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

        case 'T':
          System.out.println ("  Title command: " + data);
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

  private int findEndPtr (byte[] buffer, int ptr)
  {
    while (buffer[ptr] != (byte) 0x8D)
      ptr++;
    return ptr;
  }

  private double evaluateFunction (String function)
  {
    if (functions.containsKey (function))
      return functions.get (function);

    Range range = null;
    Matcher m = functionPattern.matcher (function);
    while (m.find ())
    {
      Address fromAddress = new Address (m.group (1), m.group (2));
      Address toAddress = new Address (m.group (3), m.group (4));
      range = new Range (fromAddress, toAddress);
    }

    double result = 0;

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
        if (cell != null && cell.hasValue () && cell.value != 0.0)
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

    functions.put (function, result);
    return result;
  }

  public double getValue (Address address)
  {
    VisicalcCell cell = sheet.get (address.sortValue);
    return cell == null ? 0.0 : cell.value;
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
    String longLine = "                                                     "
        + "                                                                 ";

    DecimalFormat nf = new DecimalFormat ("$#####0.00");
    //    NumberFormat nf = NumberFormat.getCurrencyInstance ();
    int lastRow = 0;
    int lastColumn = -1;

    for (VisicalcCell cell : sheet.values ())
    {
      while (lastRow < cell.address.row)
      {
        text.append ("\n");
        ++lastRow;
        lastColumn = -1;
      }

      while (lastColumn < cell.address.column - 1)
      {
        int width = columnWidth;
        if (columnWidths.containsKey (cell.address.column))
          width = columnWidths.get (cell.address.column);
        text.append (longLine.substring (0, width));
        ++lastColumn;
      }
      lastColumn = cell.address.column;

      int colWidth = columnWidth;
      if (columnWidths.containsKey (cell.address.column))
        colWidth = columnWidths.get (cell.address.column);

      if (cell.hasValue ())
      {
        if (defaultFormat == 'I')
        {
          String integerFormat = String.format ("%%%d.0f", colWidth);
          text.append (String.format (integerFormat, cell.getValue ()));
        }
        else if (defaultFormat == '$')
        {
          String currencyFormat = String.format ("%%%d.%ds", colWidth, colWidth);
          text.append (String.format (currencyFormat, nf.format (cell.getValue ())));
        }
        else
        {
          String numberFormat = String.format ("%%%d.3f", colWidth);
          text.append (String.format (numberFormat, cell.getValue ()));
        }
      }
      else
      {
        String format = String.format ("%%-%d.%ds", colWidth, colWidth);
        text.append (String.format (format, cell.value ()));
      }
    }
    return text.toString ();
  }

  class VisicalcCell implements Comparable<VisicalcCell>
  {
    private final Address address;
    private final VisicalcSpreadsheet parent;

    private String label;
    private double value;
    private String formula;
    private char format;
    private int width;
    private int columnWidth;
    private char repeatingChar;
    private String repeat = "";
    private boolean valid;

    public VisicalcCell (VisicalcSpreadsheet parent, Address address)
    {
      this.parent = parent;
      this.address = address;
    }

    public void doCommand (String command)
    {
      if (command.startsWith ("/"))
      {
        if (command.charAt (1) == 'F')              // format cell
        {
          format = command.charAt (2);
          if (command.length () > 3 && command.charAt (3) == '"')
            label = command.substring (4);
        }
        else if (command.charAt (1) == '-')         // repeating label
        {
          repeatingChar = command.charAt (2);
          for (int i = 0; i < 20; i++)
            repeat += repeatingChar;
        }
        else
          System.out.println ("Unknown command: " + command);
      }
      else if (command.startsWith ("\""))             // starts with a quote
        label = command.substring (1);
      else if (command.matches ("^[0-9.]+$"))         // contains only numbers or .
        this.value = Float.parseFloat (command);
      else
        formula = command;
    }

    public boolean hasValue ()
    {
      return label == null && repeatingChar == 0;
    }

    public double getValue ()
    {
      if (valid || formula == null)
        return value;

      double result = 0.0;
      double interim = 0.0;

      Matcher m = cellContents.matcher (formula);
      while (m.find ())
      {
        valid = true;
        char operator = m.group (1).isEmpty () ? '+' : m.group (1).charAt (0);

        if (m.group (3) != null)                            // address
          interim = parent.getValue (m.group (3));
        else if (m.group (4) != null)                       // constant
          try
          {
            interim = Double.parseDouble (m.group (4));
          }
          catch (NumberFormatException e)
          {
            System.out.printf ("NFE: %s%n", m.group (4));
          }
        else
          interim = parent.evaluateFunction (m.group (5));         // function

        if (operator == '+')
          result += interim;
        else if (operator == '-')
          result -= interim;
        else if (operator == '*')
          result *= interim;
        else if (operator == '/')
          result = interim == 0.0 ? 0 : result / interim;
      }

      if (valid)
      {
        value = result;
        return result;
      }

      System.out.println ("?? " + formula);

      return value;
    }

    public String value ()
    {
      if (label != null)
        return label;
      if (repeatingChar > 0)
        return repeat;
      if (formula != null)
        if (formula.length () >= 12)
          return formula.substring (0, 12);
        else
          return formula;
      return value + "";
    }

    @Override
    public String toString ()
    {
      String value = repeatingChar == 0 ? label == null
          ? formula == null ? ", Value: " + this.value : ", Frmla: " + formula
          : ", Label: " + label : ", Rpeat: " + repeatingChar;
      String format = this.format == 0 ? "" : ", Format: " + this.format;
      String width = this.width == 0 ? "" : ", Width: " + this.width;
      String columnWidth =
          this.columnWidth == 0 ? "" : ", Col Width: " + this.columnWidth;
      return String.format ("[Cell:%5s%s%s%s%s]", address, format, width, columnWidth,
                            value);
    }

    @Override
    public int compareTo (VisicalcCell o)
    {
      return address.compareTo (o.address);
    }
  }

  class Range implements Iterable<Address>
  {
    Address from, to;
    List<Address> range = new ArrayList<Address> ();

    public Range (Address from, Address to)
    {
      this.from = from;
      this.to = to;

      range.add (from);

      if (from.row == to.row)
      {
        while (from.compareTo (to) < 0)
        {
          from = from.nextColumn ();
          range.add (from);
        }
      }
      else if (from.column == to.column)
      {
        while (from.compareTo (to) < 0)
        {
          from = from.nextRow ();
          range.add (from);
        }
      }
      else
        throw new InvalidParameterException ();
    }

    @Override
    public String toString ()
    {
      return String.format ("      %s -> %s", from.text, to.text);
    }

    @Override
    public Iterator<Address> iterator ()
    {
      return range.iterator ();
    }
  }

  class Address implements Comparable<Address>
  {
    int row, column;
    int sortValue;
    String text;

    public Address (String column, String row)
    {
      set (column, row);
    }

    public Address (int column, int row)
    {
      assert column <= 64;
      assert row <= 255;
      this.row = row;
      this.column = column;
      sortValue = row * 64 + column;

      int col1 = column / 26;
      int col2 = column % 26;
      String col =
          col1 > 0 ? (char) ('@' + col1) + ('A' + col2) + "" : (char) ('A' + col2) + "";
      text = col + (row + 1);
    }

    public Address (String address)
    {
      if (address.charAt (1) < 'A')
        set (address.substring (0, 1), address.substring (1));
      else
        set (address.substring (0, 2), address.substring (2));
    }

    private void set (String sCol, String sRow)
    {
      if (sCol.length () == 1)
        column = sCol.charAt (0) - 'A';
      else if (sCol.length () == 2)
        column = (sCol.charAt (0) - '@') * 26 + sCol.charAt (1) - 'A';
      else
        System.out.println ("Bollocks");

      row = Integer.parseInt (sRow) - 1;
      sortValue = row * 64 + column;
      text = sCol + sRow;
    }

    public Address nextRow ()
    {
      Address next = new Address (column, row + 1);
      return next;
    }

    public Address nextColumn ()
    {
      Address next = new Address (column + 1, row);
      return next;
    }

    @Override
    public String toString ()
    {
      return String.format ("%s %d %d %d", text, row, column, sortValue);
    }

    @Override
    public int compareTo (Address o)
    {
      return sortValue - o.sortValue;
    }
  }
}