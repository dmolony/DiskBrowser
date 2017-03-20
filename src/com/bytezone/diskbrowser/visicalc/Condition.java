package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;

// Predicate
class Condition extends AbstractValue implements Iterable<Value>
{
  private static final String[] comparators = { "<>", "<=", ">=", "=", "<", ">" };

  private String comparator;
  private String conditionText;
  private String valueText;
  private final String fullText;

  private Expression conditionExpression;
  private Expression valueExpression;

  public Condition (Cell cell, String text)
  {
    super ("Cond");
    fullText = text;

    for (String comp : comparators)
    {
      int pos = text.indexOf (comp);
      if (pos > 0)
      {
        conditionText = text.substring (0, pos);
        conditionExpression = new Expression (cell, conditionText);
        values.add (conditionExpression);

        comparator = comp;

        valueText = text.substring (pos + comp.length ());
        valueExpression = new Expression (cell, valueText);
        values.add (valueExpression);
        break;
      }
    }

    if (comparator == null)
    {
      if (text.startsWith ("@"))
      {
        conditionText = text;
        conditionExpression = new Expression (cell, text);
        values.add (conditionExpression);

        comparator = "=";

        valueText = "1";
        valueExpression = new Expression (cell, valueText);
        values.add (valueExpression);
      }
      else
        System.out.println ("No comparator and not a function");
    }
  }

  @Override
  public void calculate ()
  {
    value = 0;

    conditionExpression.calculate ();
    valueExpression.calculate ();

    if (conditionExpression.isValueType (ValueType.ERROR)
        || valueExpression.isValueType (ValueType.ERROR))
      return;

    double conditionResult = conditionExpression.getValue ();
    double valueResult = valueExpression.getValue ();

    if (comparator.equals ("="))
      value = conditionResult == valueResult ? 1 : 0;
    else if (comparator.equals ("<>"))
      value = conditionResult != valueResult ? 1 : 0;
    else if (comparator.equals ("<"))
      value = conditionResult < valueResult ? 1 : 0;
    else if (comparator.equals (">"))
      value = conditionResult > valueResult ? 1 : 0;
    else if (comparator.equals ("<="))
      value = conditionResult <= valueResult ? 1 : 0;
    else if (comparator.equals (">="))
      value = conditionResult >= valueResult ? 1 : 0;
    else
      System.out.printf ("Unexpected comparator result [%s]%n", comparator);
  }

  String getFullText ()
  {
    return fullText;
  }

  @Override
  public String toString ()
  {
    return String.format ("[cond=%s, op:%s, value=%s]", conditionText, comparator,
        valueText);
  }

  @Override
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }
}