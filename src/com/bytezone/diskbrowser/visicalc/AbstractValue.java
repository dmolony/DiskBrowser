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
}