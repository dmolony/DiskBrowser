package com.bytezone.diskbrowser.visicalc;

public class Sum
{
  Range range;
  Sheet parent;

  public Sum (Sheet parent, String text)
  {
    this.parent = parent;
    range = parent.getRange (text);
  }

  public double getValue ()
  {
    double result = 0;

    for (Address address : range)
      result += parent.getCell (address).getValue ();

    return result;
  }
}