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

  //  @Override
  //  public boolean isValue ()
  //  {
  //    return valueType == ValueType.VALUE;
  //  }
  //
  //  @Override
  //  public boolean isError ()
  //  {
  //    return valueType == ValueType.ERROR;
  //  }
  //
  //  @Override
  //  public boolean isNotAvailable ()
  //  {
  //    return valueType == ValueType.NA;
  //  }
  //
  //  @Override
  //  public boolean isNotANumber ()
  //  {
  //    return valueType == ValueType.NAN;
  //  }
  @Override
  public boolean is (ValueType type)
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

  //  @Override
  //  public String getText ()
  //  {
  //    return isNotAvailable () ? "NA" : isError () ? "Error" : isNotANumber () ? "NaN" : "";
  //    //    return valueType == ValueType.ERROR ? "Error" : "";
  //  }

  @Override
  public String getText ()
  {
    switch (valueType)
    {
      case NA:
        return "NA";
      case ERROR:
        return "Error";
      case NAN:
        return "NaN";
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