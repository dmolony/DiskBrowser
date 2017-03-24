package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;
import java.util.regex.Pattern;

// Predicate
class Condition extends AbstractValue implements Iterable<Value>
{
  private static final Pattern cellAddress = Pattern.compile ("[A-B]?[A-Z][0-9]{1,3}");
  private static final String[] comparators = { "<>", "<=", ">=", "=", "<", ">" };

  private String comparator;
  private String conditionText;
  private String valueText;
  private final String fullText;
  //  private Address address; 

  private Expression conditionExpression;
  private Expression valueExpression;

  public Condition (Cell cell, String text)
  {
    super (cell, text);

    valueType = ValueType.BOOLEAN;
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

        //        comparator = "=";
        //
        //        valueText = "1";
        //        valueExpression = new Expression (cell, valueText);
        //        values.add (valueExpression);
      }
      else if (cellAddress.matcher (text).matches ())
      {
        conditionText = text;
        conditionExpression = new Expression (cell, text);
        conditionExpression.valueType = ValueType.BOOLEAN;
        values.add (conditionExpression);
      }
      else
      {
        System.out.println ("No comparator and not a function: " + text);
        throw new IllegalArgumentException ("No comparator and not a function: " + text);
      }
    }
  }

  @Override
  public void calculate ()
  {
    //    System.out.printf ("********Calc: %s%n", fullText);
    valueResult = ValueResult.VALID;

    conditionExpression.calculate ();
    if (!conditionExpression.isValid ())
    {
      valueResult = conditionExpression.getValueResult ();
      return;
    }

    // a boolean won't have a comparator or a valueExpression
    if (conditionExpression.getValueType () == ValueType.BOOLEAN)
    {
      bool = conditionExpression.getBoolean ();
      //      System.out.printf ("********Bool: %s%n", bool);
      return;
    }

    valueExpression.calculate ();
    if (!valueExpression.isValid ())
    {
      valueResult = valueExpression.getValueResult ();
      return;
    }

    double conditionResult = conditionExpression.getDouble ();
    double expressionResult = valueExpression.getDouble ();

    if (comparator.equals ("="))
      bool = conditionResult == expressionResult;
    else if (comparator.equals ("<>"))
      bool = conditionResult != expressionResult;
    else if (comparator.equals ("<"))
      bool = conditionResult < expressionResult;
    else if (comparator.equals (">"))
      bool = conditionResult > expressionResult;
    else if (comparator.equals ("<="))
      bool = conditionResult <= expressionResult;
    else if (comparator.equals (">="))
      bool = conditionResult >= expressionResult;
    else
      System.out.printf ("Unexpected comparator result [%s]%n", comparator);
    //    System.out.printf ("********Bool: %s%n", bool);
  }

  @Override
  public String getFullText ()
  {
    return fullText;
  }

  @Override
  public String getType ()
  {
    return "Condition";
  }

  static boolean isCondition (String text)
  {
    int ptr = 0;
    int depth = 0;
    while (ptr < text.length ())
    {
      char c = text.charAt (ptr);
      if (c == '(')
        ++depth;
      else if (c == ')')
        --depth;
      else if (depth == 0 && (c == '=' || c == '<' || c == '>'))
        return true;

      ++ptr;
    }

    return false;
  }

  @Override
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (LINE + "\n");
    text.append (String.format (FMT4, "Predicate", getFullText (), valueType,
        getValueText (this)));
    text.append (String.format (FMT4, "Left", conditionText,
        conditionExpression.getValueType (), getValueText (conditionExpression)));
    if (comparator != null)
    {
      text.append (String.format (FMT2, "Comparatr", comparator));
      text.append (String.format (FMT4, "Right", valueText,
          valueExpression.getValueType (), getValueText (valueExpression)));
    }
    //    text.append (LINE);
    return text.toString ();
  }
}