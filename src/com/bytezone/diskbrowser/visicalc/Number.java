package com.bytezone.diskbrowser.visicalc;

class Number implements Value
{
  double value;
  boolean isError;

  public Number (String text)
  {
    try
    {
      value = Double.parseDouble (text);
    }
    catch (NumberFormatException e)
    {
      isError = true;
    }
  }

  @Override
  public boolean isError ()
  {
    return isError;
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
}