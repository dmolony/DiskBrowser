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
    super ("Cell " + address.text);

    this.parent = parent;
    this.address = address;

    cellType = CellType.EMPTY;
  }

  Address getAddress ()
  {
    return address;
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
      expressionText = command;
      cellType = CellType.VALUE;
    }

    // FUTURE.VC
    if (false)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.rowKey == 67)
        expressionText = "1000";
      else if (address.rowKey == 131)
        expressionText = "10.5";
      else if (address.rowKey == 195)
        expressionText = "12";
      else if (address.rowKey == 259)
        expressionText = "8";
    }

    // IRA.VC
    if (false)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.rowKey == 66)
        expressionText = "10";
      else if (address.rowKey == 130)
        expressionText = "30";
      else if (address.rowKey == 194)
        expressionText = "65";
      else if (address.rowKey == 258)
        expressionText = "1000";
      else if (address.rowKey == 386)
        expressionText = "15";
    }

    // CARLOAN.VC
    if (false)
    {
      System.out.println ("****** Hardcoded values ******");
      if (address.rowKey == 67)
        expressionText = "9375";
      else if (address.rowKey == 131)
        expressionText = "4500";
      else if (address.rowKey == 195)
        expressionText = "24";
      else if (address.rowKey == 259)
        expressionText = "11.9";
    }
  }

  // format cell value for output
  String getText (int colWidth, char globalFormat)
  {
    switch (cellType)
    {
      case LABEL:
        return Format.justify (label, colWidth, cellFormat);

      case REPEATING_CHARACTER:
        return Format.justify (repeat, colWidth, ' ');

      case EMPTY:
        return Format.justify (empty, colWidth, ' ');

      case VALUE:
        if (!isValueType (ValueType.VALUE))
          return Format.justify (value.getText (), colWidth, 'R');

        char formatChar = cellFormat != ' ' ? cellFormat : globalFormat;
        return " " + Format.format (value, formatChar, colWidth - 1);

      default:
        assert false;
        return getText ();        // not possible
    }
  }

  @Override
  public double getValue ()
  {
    if (value == null)
      calculate ();

    return value.getValue ();
  }

  @Override
  public ValueType getValueType ()
  {
    return value.getValueType ();
  }

  @Override
  public String getText ()
  {
    if (value == null)
      calculate ();

    return value.getText ();
  }

  @Override
  public boolean isValueType (ValueType type)
  {
    if (value == null)
      calculate ();

    return value.isValueType (type);
  }

  public boolean isCellType (CellType type)
  {
    return cellType == type;
  }

  @Override
  public void calculate ()
  {
    if (value != null && value.isValueType (ValueType.VALUE))
      return;

    if (value == null)
    {
      if (expressionText == null)
        expressionText = "";

      value = new Expression (parent, expressionText).reduce ();
    }

    value.calculate ();

    return;
  }

  public String getDebugText ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (line);
    text.append ("\n");
    text.append (String.format ("| %-21s  %s  %17s |%n", address.getText (),
        address.getDetails (), "Format : " + cellFormat));
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

    return String.format ("[Cell:%5s %s]", address, contents);
  }

  @Override
  public int compareTo (Cell o)
  {
    return address.compareTo (o.address);
  }
}