package com.bytezone.diskbrowser.visicalc;

class Number implements Value
{
  private double value;
  private ValueType valueType;

  public Number (String text)
  {
    try
    {
      value = Double.parseDouble (text);
      valueType = ValueType.VALUE;
    }
    catch (NumberFormatException e)
    {
      valueType = ValueType.ERROR;
    }
  }

  @Override
  public boolean isValue ()
  {
    return valueType == ValueType.VALUE;
  }

  @Override
  public boolean isError ()
  {
    return valueType == ValueType.ERROR;
  }

  @Override
  public boolean isNotAvailable ()
  {
    return valueType == ValueType.NA;
  }

  @Override
  public String toString ()
  {
    return String.format ("Number: %f", value);
  }

  @Override
  public double getValue ()
  {
    return value;
  }

  @Override
  public String getText ()
  {
    return valueType == ValueType.ERROR ? "Error" : "";
  }

  @Override
  public Value calculate ()
  {
    return this;
  }

  @Override
  public ValueType getValueType ()
  {
    return valueType;
  }
}