package com.bytezone.diskbrowser.visicalc;

class Cell implements Comparable<Cell>, Value
{
  //  private static final Pattern cellContents =
  //      Pattern.compile ("([-+/*]?)(([A-Z]{1,2}[0-9]{1,3})|([0-9.]+)|(@[^-+/*]+))");

  final Address address;
  private final Sheet parent;

  private String label;
  private char format = ' ';
  private char repeatingChar;
  private String repeat = "";

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
    //  /F* - graph (histogram)

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
    return "?";
  }

  @Override
  public double getValue ()
  {
    if (expression == null)
    {
      if (expressionText == null)
      {
        System.out.printf ("%s null expression text %n", address);
        return 0;
      }
      //      System.out.printf ("%s Instantiating [%s]%n", address, expressionText);
      expression = new Expression (parent, expressionText);
    }
    return expression.getValue ();
  }

  @Override
  public String toString ()
  {
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