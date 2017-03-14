package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;

// Predicate
class Condition extends AbstractValue implements Iterable<Value>
{
  private static final String[] comparators = { "<>", "<=", ">=", "=", "<", ">" };

  private final Sheet parent;

  private String comparator;
  private String conditionText;
  private String valueText;
  private final String fullText;

  private Expression conditionExpression;
  private Expression valueExpression;

  public Condition (Sheet parent, Cell cell, String text)
  {
    super ("Cond");
    this.parent = parent;
    fullText = text;

    for (String comp : comparators)
    {
      int pos = text.indexOf (comp);
      if (pos > 0)
      {
        conditionText = text.substring (0, pos);
        valueText = text.substring (pos + comp.length ());
        conditionExpression = new Expression (parent, cell, conditionText);
        valueExpression = new Expression (parent, cell, valueText);
        values.add (conditionExpression);
        values.add (valueExpression);
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
    return String.format ("[cond=%s, op=%s, value=%s]", conditionText, comparator,
        valueText);
  }

  @Override
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }
}