package com.bytezone.diskbrowser.visicalc;

public class Sum extends Function
{
  Range range;
  Sheet parent;

  public Sum (Sheet parent, String text)
  {
    this.parent = parent;
    range = getRange (text);
  }

  @Override
  public double getValue ()
  {
    double result = 0;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell != null && cell.hasValue ())
        result += cell.getValue ();
    }

    return result;
  }
}