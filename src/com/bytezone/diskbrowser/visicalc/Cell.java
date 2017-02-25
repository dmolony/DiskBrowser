package com.bytezone.diskbrowser.visicalc;

class Cell extends AbstractValue implements Comparable<Cell>
{
  //  private static final DecimalFormat nf = new DecimalFormat ("#####0.00");
  private static final String line = "+----------------------------------------"
      + "--------------------------------------------+";

  private final Address address;
  private final Sheet parent;
  private CellType cellType;
  private final Format format = new Format ();
  private String expressionText;

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

    if (formatText.equals ("/TH") || formatText.equals ("/TV"))     // lock titles
      return;

    if (formatText.startsWith ("/F"))
    {
      format.cellFormat = formatText.charAt (2);
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
  String getText (int colWidth, char defaultFormat)
  {
    switch (cellType)
    {
      case LABEL:
        return format.justify (label, colWidth, format.cellFormat);

      case REPEATING_CHARACTER:
        return format.justify (repeat, colWidth, ' ');

      case EMPTY:
        return "";

      case VALUE:
        if (value == null)
          calculate ();
        return format.format (value, defaultFormat, colWidth);

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
  public Value calculate ()
  {
    if (value != null && value.isValueType (ValueType.VALUE))
      return this;

    if (value == null)
    {
      if (expressionText == null)
        expressionText = "";

      Expression expression = new Expression (parent, expressionText);
      //      value = expression.size () == 1 ? expression.get (0) : expression;
      value = expression.reduce ();
    }

    value.calculate ();

    return this;
  }

  public String getDebugText ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (line);
    text.append ("\n");
    text.append (String.format ("| %-21s  %s  %17s |%n", address.getText (),
        address.getDetails (), "Format : " + format.cellFormat));
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
          text.append (getValueText (value, 0));
        break;

      default:
        text.append ("Unknown CellType: " + cellType + "\n");
    }

    text.append (line);
    return text.toString ();
  }

  private String getValueText (Value value, int depth)
  {
    StringBuilder text = new StringBuilder ();

    String typeText = "  " + value.getTypeText ();
    if (value.isValueType (ValueType.VALUE))
    {
      String valueText = String.format ("%f", value.getValue ());
      text.append (String.format ("| %-10s : %-69s |%n", typeText, valueText));
    }
    else
      text.append (
          String.format ("| %-10s : %-69s |%n", typeText, value.getValueType ()));

    if (value instanceof Expression)
    {
      text.append (
          String.format ("| Expression : %-69s |%n", ((Expression) value).fullText ()));
      for (Value v : (Expression) value)
        text.append (getValueText (v, depth + 1));
    }
    else if (value instanceof Function)
    {
      text.append (
          String.format ("| Function   : %-69s |%n", ((Function) value).fullText));
      for (Value v : (Function) value)
        text.append (getValueText (v, depth + 1));
    }

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