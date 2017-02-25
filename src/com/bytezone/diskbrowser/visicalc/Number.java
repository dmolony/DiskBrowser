package com.bytezone.diskbrowser.visicalc;

class Number extends AbstractValue
{
  private double value;
  private ValueType valueType;

  public Number (String text)
  {
    super ("Constant");

    try
    {
      value = Double.parseDouble (text);
      valueType = ValueType.VALUE;
    }
    catch (NumberFormatException e)
    {
      valueType = ValueType.ERROR;
      e.printStackTrace ();
    }
  }

  @Override
  public boolean isValueType (ValueType type)
  {
    return valueType == type;
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
    switch (valueType)
    {
      case NA:
        return "NA";
      case ERROR:
        return "Error";
      //      case NAN:
      //        return "NaN";
      default:
        return "";
    }
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