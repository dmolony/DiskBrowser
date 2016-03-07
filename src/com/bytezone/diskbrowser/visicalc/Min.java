package com.bytezone.diskbrowser.visicalc;

public class Min extends Function
{
  Range range;
  Sheet parent;

  public Min (Sheet parent, String text)
  {
    this.parent = parent;
    range = parent.getRange (text);
  }

  @Override
  public double getValue ()
  {
    double min = Double.MAX_VALUE;
    for (Address address : range)
    {
      double value = parent.getCell (address).getValue ();
      if (value < min)
        min = value;
    }
    return min;
  }
}