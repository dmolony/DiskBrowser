package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;

class Cell implements Value, Comparable<Cell>
{
  private static final String line = "+----------------------------------------"
      + "--------------------------------------------+";
  private static final String empty = "                                        ";

  private final Address address;
  private final Sheet parent;
  private String fullText;
  private char cellFormat = ' ';
  private String repeat = "";
  private boolean calculated;

  private CellType cellType;
  private String repeatingText;       // REPEATING_CHARACTER
  private String label;               // LABEL
  private Value value;                // VALUE

  enum CellType
  {
    LABEL, FILLER, VALUE, EMPTY
  }

  public Cell (Sheet parent, Address address)
  {
    this.parent = parent;
    this.address = address;

    cellType = CellType.EMPTY;
  }

  void reset ()
  {
    calculated = false;
  }

  Cell getCell (Address address)
  {
    return parent.getCell (address);
  }

  Cell getCell (String addressText)
  {
    return parent.getCell (addressText);
  }

  boolean cellExists (Address address)
  {
    return parent.cellExists (address);
  }

  Function getFunction (String text)
  {
    return parent.getFunction (this, text);
  }

  Value getExpressionValue (String text)
  {
    return new Expression (this, text).reduce ();
  }

  boolean isCellType (CellType cellType)
  {
    return this.cellType == cellType;
  }

  Sheet getParent ()
  {
    return parent;
  }

  Address getAddress ()
  {
    return address;
  }

  String getAddressText ()
  {
    return address.getText ();
  }

  void setFormat (String formatText)
  {
    //  /FG - general
    //  /FD - default
    //  /FI - integer
    //  /F$ - two decimal places
    //  /FL - left justified
    //  /FR - right justified
    //  /F* - graph (histogram)

    if (formatText.startsWith ("/T"))             // lock titles
      return;

    if (formatText.startsWith ("/F"))
    {
      cellFormat = formatText.charAt (2);
      return;
    }

    if (formatText.startsWith ("/-"))
    {
      repeatingText = formatText.substring (2);
      for (int i = 0; i < 20; i++)
        repeat += repeatingText;
      cellType = CellType.FILLER;
      return;
    }

    System.out.printf ("Unexpected format [%s]%n", formatText);
  }

  void setValue (String command)
  {
    assert cellType == CellType.EMPTY;

    if (!command.isEmpty () && command.charAt (0) == '"')
    {
      label = command.substring (1);
      cellType = CellType.LABEL;
    }
    else
    {
      fullText = command;
      cellType = CellType.VALUE;
      try
      {
        value = new Expression (this, fullText).reduce ();
      }
      catch (IllegalArgumentException e)
      {
        value = new Error (this, "@ERROR");
      }
    }

    if (false)
      setTestData (0);
  }

  private void setTestData (int choice)
  {
    // FUTURE.VC
    if (choice == 1)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.getRowKey () == 67)
        fullText = "1000";
      else if (address.getRowKey () == 131)
        fullText = "10.5";
      else if (address.getRowKey () == 195)
        fullText = "12";
      else if (address.getRowKey () == 259)
        fullText = "8";
    }

    // IRA.VC
    if (choice == 2)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.getRowKey () == 66)
        fullText = "10";
      else if (address.getRowKey () == 130)
        fullText = "30";
      else if (address.getRowKey () == 194)
        fullText = "65";
      else if (address.getRowKey () == 258)
        fullText = "1000";
      else if (address.getRowKey () == 386)
        fullText = "15";
    }

    // CARLOAN.VC
    if (choice == 3)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.getRowKey () == 67)
        fullText = "9375";
      else if (address.getRowKey () == 131)
        fullText = "4500";
      else if (address.getRowKey () == 195)
        fullText = "24";
      else if (address.getRowKey () == 259)
        fullText = "11.9";
    }
  }

  // format cell value for output
  String getFormattedText (int colWidth, char globalFormat)
  {
    char fmtChar = cellFormat != ' ' ? cellFormat : globalFormat;

    switch (cellType)
    {
      case LABEL:
        if (fmtChar != 'R')
          fmtChar = 'L';
        return Format.justify (label, colWidth, fmtChar);

      case FILLER:
        return Format.justify (repeat, colWidth, ' ');

      case EMPTY:
        return Format.justify (empty, colWidth, ' ');

      case VALUE:
        switch (getValueResult ())
        {
          case ERROR:
          case NA:
            if (fmtChar == ' ')
              fmtChar = 'R';
            return " " + Format.justify (value.getText (), colWidth - 1, fmtChar);

          case VALID:
            switch (getValueType ())
            {
              case BOOLEAN:
                if (fmtChar != 'L')
                  fmtChar = 'R';
                return Format.justify (value.getText (), colWidth, fmtChar);

              case NUMBER:
                if (colWidth == 1)
                  return ".";
                return " " + Format.format (value, fmtChar, colWidth - 1);

              default:
                assert false;
                return "Impossible";
            }

          default:
            assert false;
            return "Impossible";
        }
      default:
        assert false;
        return "impossible";
    }
  }

  @Override
  public void calculate ()
  {
    if (cellType == CellType.VALUE && !calculated)
    {
      value.calculate ();
      calculated = true;
    }
  }

  @Override
  public boolean isValid ()
  {
    return cellType == CellType.VALUE ? value.isValid () : true;
  }

  @Override
  public ValueResult getValueResult ()
  {
    return cellType == CellType.VALUE ? value.getValueResult () : ValueResult.VALID;
  }

  @Override
  public ValueType getValueType ()
  {
    return cellType == CellType.VALUE ? value.getValueType () : ValueType.NUMBER;
  }

  @Override
  public double getDouble ()
  {
    return cellType == CellType.VALUE ? value.getDouble () : 0;
  }

  @Override
  public boolean getBoolean ()
  {
    return cellType == CellType.VALUE ? value.getBoolean () : false;
  }

  @Override
  public int size ()
  {
    return cellType == CellType.VALUE ? value.size () : 0;
  }

  @Override
  public Iterator<Value> iterator ()
  {
    return cellType == CellType.VALUE ? value.iterator () : null;
  }

  @Override
  public String getFullText ()
  {
    switch (cellType)
    {
      case LABEL:
        return "LBL : " + label;
      case FILLER:
        return "RPT : " + repeatingText;
      case EMPTY:
        return "Empty Cell";
      case VALUE:
        return value.getFullText ();
      default:
        return "impossible";
    }
  }

  @Override
  public String getType ()
  {
    return "Cell " + getAddressText ();
  }

  @Override
  public String getText ()
  {
    // cell points to another cell which is ERROR or NA
    assert cellType == CellType.VALUE;
    //    assert !value.isValid ();

    return value.getText ();
  }

  Value getValue ()
  {
    return value;
  }

  @Override
  public String toString ()
  {
    String contents = "";
    String contents2 = "";
    String valueText = "";
    String line2 = "";

    switch (cellType)
    {
      case LABEL:
        contents = label;
        break;
      case FILLER:
        contents = repeatingText;
        break;
      case EMPTY:
        contents = "";
        break;
      case VALUE:
        contents = fullText;
        valueText = ": " + value.getValueType ();
        break;
    }

    if (contents.length () > 49)
    {
      contents2 = contents.substring (49);
      contents = contents.substring (0, 49);
      line2 = String.format ("|             %-69.69s|%n", contents2);
    }
    return String.format ("%s%n| %-9.9s : %-49.49s %-18.18s |%n%s", AbstractValue.LINE,
        address.getText (), contents, cellType + valueText, line2);
  }

  @Override
  public int compareTo (Cell o)
  {
    return address.compareTo (o.address);
  }
}