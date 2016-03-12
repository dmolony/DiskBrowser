package com.bytezone.diskbrowser.visicalc;

class Number implements Value
{
  private double value;
  private boolean hasValue;

  public Number (String text)
  {
    try
    {
      this.value = Double.parseDouble (text);
      hasValue = true;
    }
    catch (NumberFormatException e)
    {
      hasValue = false;
    }
  }

  @Override
  public boolean hasValue ()
  {
    return hasValue;
  }

  @Override
  public double getValue ()
  {
    return value;
  }

  @Override
  public String getError ()
  {
    return hasValue ? "" : "@NA";
  }

  @Override
  public String toString ()
  {
    return String.format ("Number: %f", value);
  }
}