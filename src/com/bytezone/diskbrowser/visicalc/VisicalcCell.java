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
  private char format = ' ';
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
    //    System.out.printf ("Cell command:%s%n", command);
    switch (command.charAt (0))
    {
      case '/':
        if (command.charAt (1) == 'F')                // format cell
        {
          //  /FG - general
          //  /FD - default
          //  /FI - integer
          //  /F$ - dollars and cents
          //  /FL - left justified
          //  /FR - right justified
          //  /F* - graph
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

  char getFormat ()
  {
    return format;
  }

  double getValue ()
  {
    if (valid || formula == null)
      return value;

    double result = 0.0;
    double interim = 0.0;

    if (formula.startsWith ("@LOOKUP("))
    {
      int pos = formula.indexOf (',');
      String sourceText = formula.substring (8, pos);
      String rangeText = formula.substring (pos + 1, formula.length () - 1);
      VisicalcCell source = parent.getCell (new Address (sourceText));
      Range range = parent.getRange (rangeText);
      //      System.out.printf ("lookup[%s][%s]%n", source, range);

      Address target = null;
      for (Address address : range)
      {
        //        System.out.printf ("%s %s%n", source, parent.getCell (address));
        if (parent.getCell (address).value > source.value)
          break;
        target = address;
      }
      if (target != null)
      {
        value = parent.getCell (target.nextColumn ()).value;
        valid = true;
        return value;
      }
      return result;
    }

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
          System.out.printf ("NFE: %s [%s]%n", m.group (4), formula);
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
        ? formula == null ? ", Value: " + this.value : ", Formula: " + formula
        : ", Label: " + label : ", Rpeat: " + repeatingChar;
    String format = this.format == ' ' ? "" : ", Format: " + this.format;
    String width = this.width == 0 ? "" : ", Width: " + this.width;
    return String.format ("[Cell:%5s%s%s%s]", address, format, width, value);
  }

  @Override
  public int compareTo (VisicalcCell o)
  {
    return address.compareTo (o.address);
  }
}