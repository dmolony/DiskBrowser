package com.bytezone.diskbrowser.visicalc;

import java.text.DecimalFormat;

class Cell implements Comparable<Cell>, Value
{
  private static final DecimalFormat nf = new DecimalFormat ("$#####0.00");

  final Address address;
  private final Sheet parent;
  private CellType type;
  private char format = ' ';

  private char repeatingChar;
  private String repeat = "";

  private String label;

  private String expressionText;
  private Value value;

  enum CellType
  {
    LABEL, REPEATING_CHARACTER, VALUE
  }

  public Cell (Sheet parent, Address address)
  {
    this.parent = parent;
    this.address = address;
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
      this.format = format.charAt (2);
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
      if (address.sortValue == 67)
        expressionText = "1000";
      else if (address.sortValue == 131)
        expressionText = "10.5";
      else if (address.sortValue == 195)
        expressionText = "12";
      else if (address.sortValue == 259)
        expressionText = "8";

    // IRA.VC
    if (false)
      if (address.sortValue == 66)
        expressionText = "10";
      else if (address.sortValue == 130)
        expressionText = "30";
      else if (address.sortValue == 194)
        expressionText = "65";
      else if (address.sortValue == 258)
        expressionText = "1000";
      else if (address.sortValue == 386)
        expressionText = "15";

    // CARLOAN.VC
    if (false)
      if (address.sortValue == 67)
        expressionText = "9375";
      else if (address.sortValue == 131)
        expressionText = "4500";
      else if (address.sortValue == 195)
        expressionText = "24";
      else if (address.sortValue == 259)
        expressionText = "11.9";
      else if (address.sortValue == 579)
        expressionText = "D9*G5/(1-((1+G5)^-D4))";
  }

  String getText (int colWidth, char defaultFormat)
  {
    // cell may have been formatted but no value set
    if (type == null)
    {
      System.out.println (this);
      return justify ("", colWidth);
    }

    switch (type)
    {
      case LABEL:
        return justify (label, colWidth);

      case REPEATING_CHARACTER:
        return justify (repeat, colWidth);

      case VALUE:
        if (hasValue ())
          if (format == 'I')
          {
            String integerFormat = String.format ("%%%d.0f", colWidth);
            return String.format (integerFormat, getValue ());
          }
          else if (format == '$')
          {
            String currencyFormat = String.format ("%%%d.%ds", colWidth, colWidth);
            return String.format (currencyFormat, nf.format (getValue ()));
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
            String val = String.format (numberFormat, getValue ());
            while (val.endsWith ("0"))
              val = ' ' + val.substring (0, val.length () - 1);
            if (val.endsWith ("."))
              val = ' ' + val.substring (0, val.length () - 1);
            if (val.length () > colWidth)
              val = val.substring (val.length () - colWidth);
            return val;
          }
    }
    return getError ();
  }

  private String justify (String text, int colWidth)
  {
    if (format == 'R')
    {
      String labelFormat = String.format ("%%%d.%ds", colWidth, colWidth);
      return (String.format (labelFormat, text));
    }

    String labelFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
    return (String.format (labelFormat, text));
  }

  @Override
  public boolean hasValue ()
  {
    if (label != null || repeatingChar > 0)
      return false;

    if (value == null)
      createValue ();
    return value.hasValue ();
  }

  // this should be called when doing calculations
  @Override
  public double getValue ()
  {
    if (value == null)
      createValue ();
    return value.getValue ();
  }

  @Override
  public String getError ()
  {
    if (value == null)
      createValue ();
    return value.getError ();
  }

  private void createValue ()
  {
    if (expressionText == null)
    {
      System.out.printf ("%s null expression text %n", address);
      value = Function.getInstance (parent, "@ERROR()");
    }
    else
      // could use Number or Cell for simple Values
      value = new Expression (parent, expressionText);
  }

  @Override
  public String toString ()
  {
    String contents = "";
    if (type != null)
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