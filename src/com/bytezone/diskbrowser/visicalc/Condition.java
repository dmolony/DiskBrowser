package com.bytezone.diskbrowser.visicalc;

public class Condition
{
  private static String[] comparators = { "<>", "<=", ">=", "=", "<", ">" };

  private final Sheet parent;

  private String comparator;
  private String cond;
  private String value;

  private Expression expCond;
  private Expression expValue;

  public Condition (Sheet parent, String text)
  {
    this.parent = parent;

    for (String comp : comparators)
    {
      int pos = text.indexOf (comp);
      if (pos > 0)
      {
        cond = text.substring (0, pos);
        value = text.substring (pos + comp.length ());
        comparator = comp;
        break;
      }
    }

    if (comparator == null)
    {
      if (text.startsWith ("@"))
      {
        cond = text;
        value = "1";
        comparator = "=";
      }
      else
        System.out.println ("No comparator and not a function");
    }
  }

  public boolean getResult ()
  {
    if (expCond == null)
    {
      expCond = new Expression (parent, cond);
      expValue = new Expression (parent, value);
    }

    double condValue = expCond.getValue ();
    double valueValue = expValue.getValue ();

    if (comparator.equals ("="))
      return condValue == valueValue;
    else if (comparator.equals ("<>"))
      return condValue != valueValue;
    else if (comparator.equals ("<"))
      return condValue < valueValue;
    else if (comparator.equals (">"))
      return condValue > valueValue;
    else if (comparator.equals ("<="))
      return condValue <= valueValue;
    else if (comparator.equals (">="))
      return condValue >= valueValue;
    else
      System.out.printf ("Unexpected comparator result [%s]%n", comparator);

    return false;               // flag error?
  }

  @Override
  public String toString ()
  {
    return String.format ("[cond=%s, op=%s, value=%s]", cond, comparator, value);
  }
}