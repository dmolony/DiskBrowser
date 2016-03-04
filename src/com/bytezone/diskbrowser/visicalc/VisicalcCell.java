package com.bytezone.diskbrowser.visicalc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class VisicalcCell implements Comparable<VisicalcCell>
{
  private static final Pattern cellContents =
      Pattern.compile ("([-+/*]?)(([A-Z]{1,2}[0-9]{1,3})|([0-9.]+)|(@[^-+/*]+))");

  final Address address;
  private final VisicalcSpreadsheet parent;

  private String label;
  private double value;
  private String formula;
  private char format;
  private int width;
  //  private int columnWidth;
  private char repeatingChar;
  private String repeat = "";
  private boolean valid;

  public VisicalcCell (VisicalcSpreadsheet parent, Address address)
  {
    this.parent = parent;
    this.address = address;
  }

  void doCommand (String command)
  {
    switch (command.charAt (0))
    {
      case '/':
        if (command.charAt (1) == 'F')                // format cell
        {
          format = command.charAt (2);
          if (command.length () > 3 && command.charAt (3) == '"')
            label = command.substring (4);
        }
        else if (command.charAt (1) == '-')           // repeating label
        {
          repeatingChar = command.charAt (2);
          for (int i = 0; i < 20; i++)
            repeat += repeatingChar;
        }
        else
          System.out.println ("Unknown command: " + command);
        break;

      case '"':
        label = command.substring (1);
        break;

      default:
        if (command.matches ("^[0-9.]+$"))         // contains only numbers or .
          this.value = Float.parseFloat (command);
        else
          formula = command;
    }
  }

  boolean hasValue ()
  {
    return label == null && repeatingChar == 0;
  }

  double getValue ()
  {
    if (valid || formula == null)
      return value;

    double result = 0.0;
    double interim = 0.0;

    Matcher m = cellContents.matcher (formula);
    while (m.find ())
    {
      valid = true;
      char operator = m.group (1).isEmpty () ? '+' : m.group (1).charAt (0);

      if (m.group (3) != null)                                    // address
        interim = parent.getValue (m.group (3));
      else if (m.group (4) != null)                               // constant
        try
        {
          interim = Double.parseDouble (m.group (4));
        }
        catch (NumberFormatException e)
        {
          System.out.printf ("NFE: %s%n", m.group (4));
        }
      else
        interim = parent.evaluateFunction (m.group (5));         // function

      if (operator == '+')
        result += interim;
      else if (operator == '-')
        result -= interim;
      else if (operator == '*')
        result *= interim;
      else if (operator == '/')
        result = interim == 0.0 ? 0 : result / interim;
    }

    if (valid)
    {
      value = result;
      return result;
    }

    System.out.println ("?? " + formula);

    return value;
  }

  String value ()
  {
    if (label != null)
      return label;
    if (repeatingChar > 0)
      return repeat;
    if (formula != null)
      if (formula.length () >= 12)
        return formula.substring (0, 12);
      else
        return formula;
    return value + "";
  }

  @Override
  public String toString ()
  {
    String value = repeatingChar == 0 ? label == null
        ? formula == null ? ", Value: " + this.value : ", Frmla: " + formula
        : ", Label: " + label : ", Rpeat: " + repeatingChar;
    String format = this.format == 0 ? "" : ", Format: " + this.format;
    String width = this.width == 0 ? "" : ", Width: " + this.width;
    //    String columnWidth = this.columnWidth == 0 ? "" : ", Col Width: " + this.columnWidth;
    return String.format ("[Cell:%5s%s%s%s]", address, format, width, value);
  }

  @Override
  public int compareTo (VisicalcCell o)
  {
    return address.compareTo (o.address);
  }
}