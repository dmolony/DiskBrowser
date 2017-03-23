package com.bytezone.diskbrowser.visicalc;

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
    LABEL, REPEATING_CHARACTER, VALUE, EMPTY
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
      cellType = CellType.REPEATING_CHARACTER;
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

      case REPEATING_CHARACTER:
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
  public String getFullText ()
  {
    switch (cellType)
    {
      case LABEL:
        return label;
      case REPEATING_CHARACTER:
        return repeatingText;
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
    return "Cell";
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

  //  public String getDebugText ()
  //  {
  //    StringBuilder text = new StringBuilder ();
  //    text.append (line);
  //    text.append ("\n");
  //    text.append (String.format ("| %-11s         %s            Format: %s  |%n",
  //        address.getText (), address.getDetails (), cellFormat));
  //    text.append (line);
  //    text.append ("\n");
  //
  //    switch (cellType)
  //    {
  //      case LABEL:
  //        text.append (String.format ("| LABEL      : %-69s |%n", label));
  //        break;
  //
  //      case REPEATING_CHARACTER:
  //        text.append (String.format ("| REPEAT     : %-69s |%n", repeatingText));
  //        break;
  //
  //      case EMPTY:
  //        text.append (String.format ("| EMPTY      : %-69s |%n", ""));
  //        break;
  //
  //      case VALUE:
  //        text.append (String.format ("| VALUE      : %-69s |%n", expressionText));
  //        if (value == null)
  //          text.append (String.format ("| Value      : %-69s |%n", "null"));
  //        else
  //          text.append (((AbstractValue) value).getValueText (0));
  //        break;
  //
  //      default:
  //        text.append ("Unknown CellType: " + cellType + "\n");
  //    }
  //
  //    text.append (line);
  //    return text.toString ();
  //  }

  @Override
  public String toString ()
  {
    String contents = "";

    switch (cellType)
    {
      case LABEL:
        contents = "Labl: " + label;
        break;
      case REPEATING_CHARACTER:
        contents = "Rept: " + repeatingText;
        break;
      case EMPTY:
        contents = "Empty";
        break;
      case VALUE:
        switch (value.getValueType ())
        {
          case NUMBER:
            contents = "Num : " + fullText;
            break;
          case BOOLEAN:
            contents = "Bool: " + fullText;
            break;
        }
        //        contents = "Exp : " + expressionText;
        break;
    }

    return String.format ("Cell:%5s %s", address.getText (), contents);
  }

  @Override
  public int compareTo (Cell o)
  {
    return address.compareTo (o.address);
  }
}