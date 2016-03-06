package com.bytezone.diskbrowser.visicalc;

public class Count
{
  Range range;
  Sheet parent;

  public Count (Sheet parent, String text)
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
      if (cell.hasValue () && cell.getValue () != 0.0)
        result += 1;
    }

    return result;
  }
}