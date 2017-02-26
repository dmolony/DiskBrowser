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
  protected String fullText;

  private Expression conditionExpression;
  private Expression valueExpression;

  public Condition (Sheet parent, String text)
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
  public Value calculate ()
  {
    value = 0;

    if (conditionExpression == null)
    {
      conditionExpression = new Expression (parent, conditionText);
      valueExpression = new Expression (parent, valueText);

      conditionExpression.calculate ();
      valueExpression.calculate ();

      values.add (conditionExpression);
      values.add (valueExpression);
    }

    if (conditionExpression.isValueType (ValueType.ERROR)
        || valueExpression.isValueType (ValueType.ERROR))
      return this;

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

    return this;
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