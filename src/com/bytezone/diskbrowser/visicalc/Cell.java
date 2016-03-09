package com.bytezone.diskbrowser.visicalc;

class Cell implements Comparable<Cell>, Value
{
  //  private static final Pattern cellContents =
  //      Pattern.compile ("([-+/*]?)(([A-Z]{1,2}[0-9]{1,3})|([0-9.]+)|(@[^-+/*]+))");

  final Address address;
  private final Sheet parent;

  private String label;
  //  private double value;
  //  private String formulaText;

  private char format = ' ';
  private char repeatingChar;
  private String repeat = "";
  //  private boolean valid;

  private String expressionText;
  private Expression expression;

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
    //  /F* - graph
    if (format.startsWith ("/F"))
      this.format = format.charAt (2);
    else if (format.startsWith ("/-"))
    {
      repeatingChar = format.charAt (2);
      for (int i = 0; i < 20; i++)
        repeat += repeatingChar;
    }
    else
      System.out.printf ("Unexpected format [%s]%n", format);
  }

  void doCommand (String command)
  {
    switch (command.charAt (0))
    {
      case '"':
        label = command.substring (1);
        break;

      default:
        expressionText = command;
    }

    // FUTURE.VC
    if (address.sortValue == 67)
      expressionText = "50";
    if (address.sortValue == 131)
      expressionText = ".04";
    if (address.sortValue == 195)
      expressionText = "12";
    if (address.sortValue == 259)
      expressionText = "5";
  }

  boolean hasValue ()
  {
    return expressionText != null;
  }

  char getFormat ()
  {
    return format;
  }

  String getText ()
  {
    if (label != null)
      return label;
    if (repeatingChar > 0)
      return repeat;
    return "bollocks";
  }

  @Override
  public double getValue ()
  {
    if (expression == null)
      expression = new Expression (parent, expressionText);
    return expression.getValue ();
    //    if (valid || formulaText == null)
    //      return value;
    //
    //    double result = 0.0;
    //    double interim = 0.0;
    //
    //    if (formulaText.startsWith ("@LOOKUP("))
    //    {
    //      Lookup lookup = new Lookup (parent, formulaText);
    //      return lookup.getValue ();
    //    }
    //
    //    System.out.printf ("Matching:[%s]%n", formulaText);
    // [@IF(@ISERROR(BK24),0,BK24)]
    // [@IF(D4=0,0,1)]
    // [@IF(D4=0,0,B32+1)]
    // [@IF(D4=0,0,1+(D3/100/D4)^D4-1*100)]
    // [@SUM(C4...F4)]
    // [+C4-@SUM(C5...C12)]
    // [+D5/100/12]
    // [.3*(B4+B7+B8+B9)]
    // [+N12+(P12*(.2*K12+K9-O12))]

    //    Matcher m = cellContents.matcher (formulaText);
    //    while (m.find ())
    //    {
    //      valid = true;
    //      char operator = m.group (1).isEmpty () ? '+' : m.group (1).charAt (0);
    //
    //      if (m.group (3) != null)                                    // address
    //      {
    //        Address address = new Address (m.group (3));
    //        Cell cell = parent.getCell (address);
    //        if (cell != null)
    //          interim = cell.getValue ();
    //      }
    //      else if (m.group (4) != null)                               // constant
    //        try
    //        {
    //          interim = Double.parseDouble (m.group (4));
    //        }
    //        catch (NumberFormatException e)
    //        {
    //          System.out.printf ("NFE: %s [%s]%n", m.group (4), formulaText);
    //        }
    //      else
    //      {
    //        //        interim = parent.evaluateFunction (m.group (5));         // function
    //        Function function = Function.getInstance (parent, m.group (5));
    //        if (function != null)
    //          interim = function.getValue ();
    //      }
    //
    //      if (operator == '+')
    //        result += interim;
    //      else if (operator == '-')
    //        result -= interim;
    //      else if (operator == '*')
    //        result *= interim;
    //      else if (operator == '/')
    //        result = interim == 0.0 ? 0 : result / interim;
    //    }
    //
    //    if (valid)
    //    {
    //      value = result;
    //      return result;
    //    }
    //
    //    System.out.println ("?? " + formulaText);
    //
    //    return value;
  }

  @Override
  public String toString ()
  {
    //    String value = repeatingChar == 0
    //        ? label == null ? formulaText == null ? ", Value  : " + this.value
    //            : ", Formula: " + formulaText : ", Label  : " + label
    //        : ", Repeat : " + repeatingChar;
    String contents = "";
    if (label != null)
      contents = "Labl: " + label;
    else if (repeatingChar != 0)
      contents = "Rept: " + repeatingChar;
    else if (expressionText != null)
      contents = "Exp : " + expressionText;
    return String.format ("[Cell:%5s %s]", address, contents);
  }

  @Override
  public int compareTo (Cell o)
  {
    return address.compareTo (o.address);
  }
}