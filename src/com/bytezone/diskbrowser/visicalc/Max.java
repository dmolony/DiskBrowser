package com.bytezone.diskbrowser.visicalc;

public class Max
{
  Range range;
  Sheet parent;

  public Max (Sheet parent, String text)
  {
    this.parent = parent;
    range = parent.getRange (text);
  }

  public double getValue ()
  {
    double max = Double.MIN_VALUE;
    for (Address address : range)
    {
      double value = parent.getCell (address).getValue ();
      if (value > max)
        max = value;
    }
    return max;
  }

}