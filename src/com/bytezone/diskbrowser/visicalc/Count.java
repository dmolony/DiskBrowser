package com.bytezone.diskbrowser.visicalc;

public class Count extends Function
{
  Range range;
  Sheet parent;

  public Count (Sheet parent, String text)
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
      if (cell != null && cell.hasValue () && cell.getValue () != 0.0)
        result += 1;
    }

    return result;
  }
}