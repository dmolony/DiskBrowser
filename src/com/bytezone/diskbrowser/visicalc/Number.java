package com.bytezone.diskbrowser.visicalc;

class Number extends AbstractValue
{
  public Number (String text)
  {
    super ("Constant");

    try
    {
      value = Double.parseDouble (text);
      isVolatile = false;
    }
    catch (NumberFormatException e)
    {
      valueType = ValueType.ERROR;
      e.printStackTrace ();
    }
  }

  @Override
  public String getText ()
  {
    return value + "";
  }

  @Override
  public String toString ()
  {
    return String.format ("Number: %f", value);
  }
}