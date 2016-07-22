package com.bytezone.diskbrowser.visicalc;

import java.text.DecimalFormat;

class Cell implements Comparable<Cell>, Value
{
  private static final DecimalFormat nf = new DecimalFormat ("$#####0.00");

  final Address address;
  private final Sheet parent;
  private CellType type;
  private char cellFormat = ' ';

  private char repeatingChar;
  private String repeat = "";

  private String label;

  private String expressionText;
  private Value value;
  private ValueType valueType;

  enum CellType
  {
    LABEL, REPEATING_CHARACTER, VALUE
  }

  public Cell (Sheet parent, Address address)
  {
    this.parent = parent;
    this.address = address;
    type = CellType.VALUE;            // default to VALUE, formatting may change it
  }

  @Override
  public boolean isValue ()
  {
    return type == CellType.VALUE;
  }

  void format (String format)
  {
    //  /FG - general
    //  /FD - default
    //  /FI - integer
    //  /F$ - dollars and cents
    //  /FL - left justified
    //  /FR - right justified
    //  /F* - graph (histogram)

    if (format.startsWith ("/F"))
      this.cellFormat = format.charAt (2);
    else if (format.startsWith ("/-"))
    {
      repeatingChar = format.charAt (2);
      for (int i = 0; i < 20; i++)
        repeat += repeatingChar;
      type = CellType.REPEATING_CHARACTER;
    }
    else
      System.out.printf ("Unexpected format [%s]%n", format);
  }

  void setValue (String command)
  {
    if (command.charAt (0) == '"')
    {
      label = command.substring (1);
      type = CellType.LABEL;
    }
    else
    {
      expressionText = command;
      type = CellType.VALUE;
    }

    // FUTURE.VC
    if (false)
      if (address.rowKey == 67)
        expressionText = "1000";
      else if (address.rowKey == 131)
        expressionText = "10.5";
      else if (address.rowKey == 195)
        expressionText = "12";
      else if (address.rowKey == 259)
        expressionText = "8";

    // IRA.VC
    if (false)
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

    // CARLOAN.VC
    if (false)
      if (address.rowKey == 67)
        expressionText = "9375";
      else if (address.rowKey == 131)
        expressionText = "4500";
      else if (address.rowKey == 195)
        expressionText = "24";
      else if (address.rowKey == 259)
        expressionText = "11.9";
  }

  String getText (int colWidth, char defaultFormat)
  {
    char format = cellFormat != ' ' ? cellFormat : defaultFormat;

    switch (type)
    {
      case LABEL:
        return justify (label, colWidth, format);

      case REPEATING_CHARACTER:
        return justify (repeat, colWidth, format);

      case VALUE:
        if (value.isError () || value.isNotAvailable () || value.isNotANumber ())
          return justify (value.getText (), colWidth, format);

        Double thisValue = value.getValue ();

        if (format == 'I')
        {
          String integerFormat = String.format ("%%%d.0f", colWidth);
          return String.format (integerFormat, thisValue);
        }
        else if (format == '$')
        {
          String currencyFormat = String.format ("%%%d.%ds", colWidth, colWidth);
          return String.format (currencyFormat, nf.format (thisValue));
        }
        else if (format == '*')
        {
          String graphFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
          // this is not finished
          return String.format (graphFormat, "********************");
        }
        else
        {
          // this could be improved
          String numberFormat = String.format ("%%%d.3f", colWidth + 4);
          String val = String.format (numberFormat, thisValue);
          while (val.endsWith ("0"))
            val = ' ' + val.substring (0, val.length () - 1);
          if (val.endsWith ("."))
            val = ' ' + val.substring (0, val.length () - 1);
          if (val.length () > colWidth)
            val = val.substring (val.length () - colWidth);
          return val;
        }
    }
    return getText ();
  }

  private String justify (String text, int colWidth, char format)
  {
    // right justify
    if (format == 'R' || format == '$' || format == 'I')
    {
      String labelFormat = String.format ("%%%d.%ds", colWidth, colWidth);
      return (String.format (labelFormat, text));
    }

    // left justify
    String labelFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
    return (String.format (labelFormat, text));
  }

  @Override
  public double getValue ()
  {
    assert type == CellType.VALUE;
    return value.getValue ();
  }

  @Override
  public ValueType getValueType ()
  {
    return valueType;
  }

  @Override
  public String getText ()
  {
    assert isValue () : "Cell type: " + type;
    return value.getText ();
  }

  @Override
  public boolean isError ()
  {
    //    assert isValue () : "Cell type: " + type;
    return value.isError ();
  }

  @Override
  public boolean isNotAvailable ()
  {
    //    assert type == CellType.VALUE : "Cell type: " + type;
    if (!isValue ())
      return true;
    return value.isNotAvailable ();
  }

  @Override
  public boolean isNotANumber ()
  {
    //    assert type == CellType.VALUE : "Cell type: " + type;
    //    if (!isValue ())
    //      return true;
    return value.isNotANumber ();
  }

  @Override
  public Value calculate ()
  {
    if (!isValue ())
    {
      //      System.out.println (value);
      return this;
    }
    assert isValue () : "Cell type: " + type + " @ " + address;
    if (expressionText == null)
    {
      System.out.printf ("%s null expression text %n", address);
      value = Function.getInstance (parent, "@ERROR");
      valueType = ValueType.ERROR;
    }
    else
    {
      // should use Number or Cell or Function for simple Values
      value = new Expression (parent, expressionText);
      value.calculate ();
      valueType = value.getValueType ();
    }
    return this;
  }

  @Override
  public String toString ()
  {
    String contents = "";

    switch (type)
    {
      case LABEL:
        contents = "Labl: " + label;
        break;
      case REPEATING_CHARACTER:
        contents = "Rept: " + repeatingChar;
        break;
      case VALUE:
        contents = "Exp : " + expressionText;
        break;
    }

    return String.format ("[Cell:%5s %s]", address, contents);
  }

  @Override
  public int compareTo (Cell o)
  {
    return address.compareTo (o.address);
  }
}