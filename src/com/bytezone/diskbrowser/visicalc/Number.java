package com.bytezone.diskbrowser.visicalc;

public class Number implements Value
{
  private final double value;

  public Number (double value)
  {
    this.value = value;
  }

  @Override
  public double getValue ()
  {
    return value;
  }

  @Override
  public String toString ()
  {
    return String.format ("Number: %f", value);
  }
}