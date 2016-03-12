package com.bytezone.diskbrowser.visicalc;

class Count extends Function
{
  private final Range range;
  private boolean hasChecked;
  private double count = 0;

  public Count (Sheet parent, String text)
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
    return hasValue () ? count : 0;
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
        if (cell.getValue () != 0.0)
          count++;
      }
    }
  }
}