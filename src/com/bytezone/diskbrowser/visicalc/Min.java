package com.bytezone.diskbrowser.visicalc;

public class Min
{
  Range range;
  Sheet parent;

  public Min (Sheet parent, String text)
  {
    this.parent = parent;
    range = parent.getRange (text);
  }

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