package com.bytezone.diskbrowser.visicalc;

class Max extends Function
{
  private final Range range;
  private boolean hasChecked;
  private double max = Double.MIN_VALUE;

  public Max (Sheet parent, String text)
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
    return hasValue () ? max : 0;
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
        if (value > max)
          max = value;
      }
    }
  }
}