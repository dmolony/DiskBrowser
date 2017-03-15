package com.bytezone.diskbrowser.visicalc;

class Cell extends AbstractValue implements Comparable<Cell>
{
  private static final String line = "+----------------------------------------"
      + "--------------------------------------------+";
  private static final String empty = "                                        ";

  private final Address address;
  private final Sheet parent;
  private CellType cellType;
  private String expressionText;
  private char cellFormat = ' ';

  private String repeatingText;
  private String repeat = "";
  private String label;
  private Value value;

  enum CellType
  {
    LABEL, REPEATING_CHARACTER, VALUE, EMPTY
  }

  public Cell (Sheet parent, Address address)
  {
    super ("Cell " + address.getText ());

    this.parent = parent;
    this.address = address;

    cellType = CellType.EMPTY;
    isVolatile = false;
  }

  boolean isCellType (CellType cellType)
  {
    return this.cellType == cellType;
  }

  @Override
  public boolean isVolatile ()
  {
    return isVolatile;
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
      isVolatile = false;
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
      isVolatile = false;
    }
    else
    {
      try
      {
        expressionText = command;
        value = new Expression (parent, this, expressionText).reduce ();
        cellType = CellType.VALUE;
        isVolatile = true;
      }
      catch (IllegalArgumentException e)
      {
        System.out.println ("ignoring error: " + command);
      }
    }

    // FUTURE.VC
    if (false)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.getRowKey () == 67)
        expressionText = "1000";
      else if (address.getRowKey () == 131)
        expressionText = "10.5";
      else if (address.getRowKey () == 195)
        expressionText = "12";
      else if (address.getRowKey () == 259)
        expressionText = "8";
    }

    // IRA.VC
    if (false)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.getRowKey () == 66)
        expressionText = "10";
      else if (address.getRowKey () == 130)
        expressionText = "30";
      else if (address.getRowKey () == 194)
        expressionText = "65";
      else if (address.getRowKey () == 258)
        expressionText = "1000";
      else if (address.getRowKey () == 386)
        expressionText = "15";
    }

    // CARLOAN.VC
    if (false)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.getRowKey () == 67)
        expressionText = "9375";
      else if (address.getRowKey () == 131)
        expressionText = "4500";
      else if (address.getRowKey () == 195)
        expressionText = "24";
      else if (address.getRowKey () == 259)
        expressionText = "11.9";
    }
  }

  // format cell value for output
  String getText (int colWidth, char globalFormat)
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
        if (!isValueType (ValueType.VALUE))
        {
          if (fmtChar == ' ')
            fmtChar = 'R';
          return " " + Format.justify (value.getText (), colWidth - 1, fmtChar);
        }

        return " " + Format.format (value, fmtChar, colWidth - 1);

      default:
        assert false;
        return "Impossible";
    }
  }

  @Override
  public double getValue ()
  {
    return cellType == CellType.VALUE ? value.getValue () : 0;
  }

  @Override
  public ValueType getValueType ()
  {
    return cellType == CellType.VALUE ? value.getValueType () : ValueType.VALUE;
  }

  @Override
  public String getText ()
  {
    if (cellType == CellType.EMPTY)
      return "MPT";

    if (cellType == CellType.LABEL)
      return "LBL";

    if (cellType == CellType.REPEATING_CHARACTER)
      return "RPT";

    assert cellType == CellType.VALUE;
    return value.getText ();
  }

  @Override
  public boolean isValueType (ValueType type)
  {
    return type == getValueType ();
  }

  @Override
  public void calculate ()
  {
    if (cellType == CellType.VALUE)
    {
      if (isVolatile)
      {
        value.calculate ();
        isVolatile = value.isVolatile ();
      }
    }
  }

  public String getDebugText ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (line);
    text.append ("\n");
    text.append (String.format ("| %-11s  %s    Volatile: %1.1s    Format: %s  |%n",
        address.getText (), address.getDetails (), isVolatile, cellFormat));
    text.append (line);
    text.append ("\n");

    switch (cellType)
    {
      case LABEL:
        text.append (String.format ("| LABEL      : %-69s |%n", label));
        break;

      case REPEATING_CHARACTER:
        text.append (String.format ("| REPEAT     : %-69s |%n", repeatingText));
        break;

      case EMPTY:
        text.append (String.format ("| EMPTY      : %-69s |%n", ""));
        break;

      case VALUE:
        text.append (String.format ("| VALUE      : %-69s |%n", expressionText));
        if (value == null)
          text.append (String.format ("| Value      : %-69s |%n", "null"));
        else
          text.append (((AbstractValue) value).getValueText (0));
        break;

      default:
        text.append ("Unknown CellType: " + cellType + "\n");
    }

    text.append (line);
    return text.toString ();
  }

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
      case VALUE:
        contents = "Exp : " + expressionText;
        break;
      case EMPTY:
        contents = "Empty";
    }

    return String.format ("[Cell:%5s %s %s]", address, contents,
        isVolatile ? "volatile" : "fixed");
  }

  @Override
  public int compareTo (Cell o)
  {
    return address.compareTo (o.address);
  }
}