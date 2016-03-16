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
    }
    catch (NumberFormatException e)
    {
      valueType = ValueType.ERROR;
    }
  }

  @Override
  public boolean isError ()
  {
    return valueType == ValueType.ERROR;
  }

  @Override
  public boolean isNaN ()
  {
    return Double.isNaN (value);
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
    return null;
  }

  @Override
  public void calculate ()
  {
  }

  @Override
  public ValueType getValueType ()
  {
    return null;
  }
}