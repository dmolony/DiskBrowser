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
    {
      Cell cell = parent.getCell (address);
      if (cell != null && cell.hasValue ())
        result += cell.getValue ();
    }

    return result;
  }
}