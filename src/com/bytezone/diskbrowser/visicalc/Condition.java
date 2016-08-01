package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Value.ValueType;

// Predicate
class Condition
{
  private static final String[] comparators = { "<>", "<=", ">=", "=", "<", ">" };

  private final Sheet parent;

  private String comparator;
  private String conditionText;
  private String valueText;

  private Expression conditionExpression;
  private Expression valueExpression;

  public Condition (Sheet parent, String text)
  {
    this.parent = parent;

    for (String comp : comparators)
    {
      int pos = text.indexOf (comp);
      if (pos > 0)
      {
        conditionText = text.substring (0, pos);
        valueText = text.substring (pos + comp.length ());
        comparator = comp;
        break;
      }
    }

    if (comparator == null)
    {
      if (text.startsWith ("@"))
      {
        conditionText = text;
        valueText = "1";
        comparator = "=";
      }
      else
        System.out.println ("No comparator and not a function");
    }
  }

  public boolean getResult ()
  {
    if (conditionExpression == null)
    {
      conditionExpression = new Expression (parent, conditionText);
      valueExpression = new Expression (parent, valueText);

      conditionExpression.calculate ();
      valueExpression.calculate ();
    }

    if (conditionExpression.isValueType (ValueType.ERROR) || valueExpression.isValueType (ValueType.ERROR))
      return false;

    double conditionResult = conditionExpression.getValue ();
    double valueResult = valueExpression.getValue ();

    if (comparator.equals ("="))
      return conditionResult == valueResult;
    else if (comparator.equals ("<>"))
      return conditionResult != valueResult;
    else if (comparator.equals ("<"))
      return conditionResult < valueResult;
    else if (comparator.equals (">"))
      return conditionResult > valueResult;
    else if (comparator.equals ("<="))
      return conditionResult <= valueResult;
    else if (comparator.equals (">="))
      return conditionResult >= valueResult;
    else
      System.out.printf ("Unexpected comparator result [%s]%n", comparator);

    return false;               // flag error?
  }

  @Override
  public String toString ()
  {
    return String.format ("[cond=%s, op=%s, value=%s]", conditionText, comparator,
                          valueText);
  }
}