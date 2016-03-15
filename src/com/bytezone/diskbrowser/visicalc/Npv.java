package com.bytezone.diskbrowser.visicalc;

public class Npv extends Function
{
  private final String valueText;
  private final String rangeText;

  private final Expression valueExp;
  private final Range range;

  private boolean hasChecked;
  private final double value = 0;

  Npv (Sheet parent, String text)
  {
    super (parent, text);

    int pos = text.indexOf (',');
    valueText = text.substring (8, pos);
    rangeText = text.substring (pos + 1, text.length () - 1);

    valueExp = new Expression (parent, valueText);
    range = getRange (rangeText);
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
    return hasValue () ? value : 0;
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
        // calculate something
      }
    }
  }
}