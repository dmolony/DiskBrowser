package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractValue implements Value
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
        return "Error";
      default:
        return "";
    }
  }

  String getValueText (Value value, int depth)
  {
    StringBuilder text = new StringBuilder ();

    String typeText = "  " + value.getTypeText ();
    if (value.isValueType (ValueType.VALUE))
    {
      String valueText = String.format ("%f", value.getValue ());
      text.append (String.format ("| %-10s : %-69s |%n", typeText, valueText));
    }
    else
      text.append (
          String.format ("| %-10s : %-69s |%n", typeText, value.getValueType ()));

    if (value instanceof Expression)
    {
      text.append (
          String.format ("| Expression : %-69s |%n", ((Expression) value).fullText ()));
      for (Value v : (Expression) value)
        text.append (getValueText (v, depth + 1));
    }
    else if (value instanceof Function)
    {
      text.append (
          String.format ("| Function   : %-69s |%n", ((Function) value).fullText));
      for (Value v : (Function) value)
        text.append (getValueText (v, depth + 1));
    }
    else if (value instanceof Condition)
    {
      text.append (
          String.format ("| Condition  : %-69s |%n", ((Condition) value).fullText));
      for (Value v : (Condition) value)
        text.append (getValueText (v, depth + 1));
    }

    return text.toString ();
  }
}