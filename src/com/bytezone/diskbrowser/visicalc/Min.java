package com.bytezone.diskbrowser.visicalc;

public class Min extends Function
{
  private final Range range;
  private boolean hasChecked;
  private double min = Double.MAX_VALUE;

  public Min (Sheet parent, String text)
  {
    super (parent, text);
    range = getRange (text);
  }

  @Override
  public boolean hasValue ()
  {
    if (!hasChecked)
      calculate ();
    return hasValue;
  }

  @Override
  public double getValue ()
  {
    if (!hasChecked)
      calculate ();
    return hasValue ? min : 0;
  }

  private void calculate ()
  {
    hasChecked = true;
    hasValue = false;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell != null && cell.hasValue ())
      {
        hasValue = true;
        double value = cell.getValue ();
        if (value < min)
          min = value;
      }
    }
  }
}