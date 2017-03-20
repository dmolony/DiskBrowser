package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractValue implements Value, Iterable<Value>
{
  protected final String typeText;
  protected double value;
  protected ValueType valueType = ValueType.VALUE;
  protected List<Value> values = new ArrayList<Value> ();

  public AbstractValue (String typeText)
  {
    this.typeText = typeText;
  }

  @Override
  public String getTypeText ()
  {
    return typeText;
  }

  @Override
  public ValueType getValueType ()
  {
    return valueType;
  }

  @Override
  public boolean isValueType (ValueType type)
  {
    return valueType == type;
  }

  @Override
  public double getValue ()
  {
    return value;
  }

  @Override
  public String getText ()
  {
    switch (valueType)
    {
      case NA:
        return "NA";
      case ERROR:
        return "ERROR";
      default:
        return "";
    }
  }

  @Override
  public void calculate ()
  {
  }

  @Override
  public boolean isBoolean ()
  {
    return false;
  }

  @Override
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }

  // for debugging
  String getValueText (int depth)
  {
    StringBuilder text = new StringBuilder ();

    String typeText = "  " + getTypeText ();
    if (isValueType (ValueType.VALUE))
    {
      String valueText = String.format ("%f", getValue ());
      text.append (String.format ("| %-10s : %-69s |%n", typeText, valueText));
    }
    else
      text.append (String.format ("| %-10s : %-69s |%n", typeText, getValueType ()));

    if (this instanceof Expression)
    {
      text.append (
          String.format ("| Expression : %-69s |%n", ((Expression) this).fullText ()));
      for (Value v : (Expression) this)
        text.append (((AbstractValue) v).getValueText (depth + 1));
    }
    else if (this instanceof Function)
    {
      text.append (
          String.format ("| Function   : %-69s |%n", ((Function) this).fullText));
      for (Value v : (Function) this)
        text.append (((AbstractValue) v).getValueText (depth + 1));
    }
    else if (this instanceof Condition)
    {
      text.append (
          String.format ("| Condition  : %-69s |%n", ((Condition) this).getFullText ()));
      for (Value v : (Condition) this)
        text.append (((AbstractValue) v).getValueText (depth + 1));
    }

    return text.toString ();
  }
}