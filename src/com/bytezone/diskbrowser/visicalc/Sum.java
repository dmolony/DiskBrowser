package com.bytezone.diskbrowser.visicalc;

public class Sum extends Function
{
  Range range;

  public Sum (Sheet parent, String text)
  {
    super (parent, text);
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