package com.bytezone.diskbrowser.visicalc;

public class Min extends Function
{
  Range range;

  public Min (Sheet parent, String text)
  {
    super (parent, text);

    range = getRange (text);
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