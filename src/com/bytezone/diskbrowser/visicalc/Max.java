package com.bytezone.diskbrowser.visicalc;

class Max extends Function
{
  private final Range range;

  public Max (Sheet parent, String text)
  {
    super (parent, text);
    range = getRange (text);
  }

  @Override
  public void calculate ()
  {
    value = Double.MIN_VALUE;
    isError = false;
    int totalChecked = 0;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null)
        continue;

      if (cell.isError () || cell.isNaN ())
      {
        isError = true;
        break;
      }

      double temp = cell.getValue ();
      if (temp > value)
        value = temp;
      totalChecked++;
    }

    if (totalChecked == 0)
      isError = true;
  }
}