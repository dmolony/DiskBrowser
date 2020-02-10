package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;

// -----------------------------------------------------------------------------------//
class Cell implements Value, Comparable<Cell>
// -----------------------------------------------------------------------------------//
{
  private static final String empty = "                                        ";

  private final Address address;
  private final Sheet parent;
  private String fullText;
  private char cellFormat = ' ';
  private String repeat = "";
  private boolean calculated;

  private CellType cellType;
  private String repeatingText;       // CellType.FILLER
  private String label;               // CellType.LABEL
  private Value value;                // CellType.VALUE

  enum CellType
  {
    LABEL, FILLER, VALUE, EMPTY
  }

  // ---------------------------------------------------------------------------------//
  Cell (Sheet parent, Address address)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;
    this.address = address;

    cellType = CellType.EMPTY;
  }

  // ---------------------------------------------------------------------------------//
  void setFormat (String formatText)
  // ---------------------------------------------------------------------------------//
  {
    if (formatText.startsWith ("/T"))             // lock titles
      return;

    if (formatText.startsWith ("/F"))
    {
      cellFormat = formatText.charAt (2);
      return;
    }

    if (formatText.startsWith ("/-"))
    {
      assert cellType == CellType.EMPTY;
      repeatingText = formatText.substring (2);

      for (int i = 0; i < 20; i++)
        repeat += repeatingText;
      cellType = CellType.FILLER;

      return;
    }

    System.out.printf ("Unexpected format [%s]%n", formatText);
  }

  // ---------------------------------------------------------------------------------//
  void setValue (String valueText)
  // ---------------------------------------------------------------------------------//
  {
    assert cellType == CellType.EMPTY;

    if (!valueText.isEmpty () && valueText.charAt (0) == '"')
    {
      label = valueText.substring (1);
      cellType = CellType.LABEL;
    }
    else
    {
      fullText = valueText;
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

  // ---------------------------------------------------------------------------------//
  void reset ()
  // ---------------------------------------------------------------------------------//
  {
    calculated = false;
  }

  // ---------------------------------------------------------------------------------//
  Value getExpressionValue (String text)
  // ---------------------------------------------------------------------------------//
  {
    return new Expression (this, text).reduce ();
  }

  // ---------------------------------------------------------------------------------//
  boolean isCellType (CellType cellType)
  // ---------------------------------------------------------------------------------//
  {
    return this.cellType == cellType;
  }

  // ---------------------------------------------------------------------------------//
  Address getAddress ()
  // ---------------------------------------------------------------------------------//
  {
    return address;
  }

  // ---------------------------------------------------------------------------------//
  // format cell value for output
  String getFormattedText (int colWidth, char globalFormat)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  //                              Sheet convenience methods
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  Cell getCell (Address address)
  // ---------------------------------------------------------------------------------//
  {
    return parent.getCell (address);
  }

  // ---------------------------------------------------------------------------------//
  Cell getCell (String addressText)
  // ---------------------------------------------------------------------------------//
  {
    return parent.getCell (addressText);
  }

  // ---------------------------------------------------------------------------------//
  boolean cellExists (Address address)
  // ---------------------------------------------------------------------------------//
  {
    return parent.cellExists (address);
  }

  // ---------------------------------------------------------------------------------//
  Function getFunction (String text)
  // ---------------------------------------------------------------------------------//
  {
    return parent.getFunction (this, text);
  }

  // ---------------------------------------------------------------------------------//
  //                              Value interface methods
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  @Override
  public void calculate ()
  // ---------------------------------------------------------------------------------//
  {
    if (cellType == CellType.VALUE && !calculated)
    {
      value.calculate ();
      calculated = true;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isValid ()
  // ---------------------------------------------------------------------------------//
  {
    return cellType == CellType.VALUE ? value.isValid () : true;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public ValueType getValueType ()
  // ---------------------------------------------------------------------------------//
  {
    return cellType == CellType.VALUE ? value.getValueType () : ValueType.NUMBER;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public ValueResult getValueResult ()
  // ---------------------------------------------------------------------------------//
  {
    return cellType == CellType.VALUE ? value.getValueResult () : ValueResult.VALID;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public double getDouble ()
  // ---------------------------------------------------------------------------------//
  {
    return cellType == CellType.VALUE ? value.getDouble () : 0;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean getBoolean ()
  // ---------------------------------------------------------------------------------//
  {
    return cellType == CellType.VALUE ? value.getBoolean () : false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return cellType == CellType.VALUE ? value.getText () : "???";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getFullText ()
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  @Override
  public String getType ()
  // ---------------------------------------------------------------------------------//
  {
    return "Cell " + address.getText ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int size ()
  // ---------------------------------------------------------------------------------//
  {
    return cellType == CellType.VALUE ? value.size () : 0;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<Value> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    return cellType == CellType.VALUE ? value.iterator () : null;
  }

  // ---------------------------------------------------------------------------------//
  //                          end of Value interface methods
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  private void setTestData (int choice)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  @Override
  public int compareTo (Cell o)
  // ---------------------------------------------------------------------------------//
  {
    return address.compareTo (o.address);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    String contents = "";
    String contents2 = "";
    String valueTypeText = "";
    String valueText = "";
    String line2 = "";
    String rest = "";

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
        valueTypeText = value.getValueType () + "";
        rest = value.toString ();
        valueText = value.getText ();
        break;
    }

    if (contents.length () > 39)
    {
      contents2 = contents.substring (39);
      contents = contents.substring (0, 39);
      line2 = String.format ("|             %-70.70s|%n", contents2);
    }

    String single = String.format (AbstractValue.FMT5, address.getText (), contents,
        cellType, valueTypeText, valueText);
    return String.format ("%s%n%s%s%s", AbstractValue.LINE, single, line2, rest);
  }
}