package com.bytezone.diskbrowser.visicalc;

class Min extends Function
{
  private final Range range;

  public Min (Sheet parent, String text)
  {
    super (parent, text);
    range = getRange (text);
  }

  @Override
  public void calculate ()
  {
    value = Double.MAX_VALUE;
    int totalChecked = 0;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null)
        continue;

      if (cell.isError () || cell.isNaN ())
      {
        valueType = ValueType.ERROR;
        return;
      }

      double temp = cell.getValue ();
      if (temp < value)
        value = temp;
      totalChecked++;
    }

    if (totalChecked == 0)
      valueType = ValueType.ERROR;
    else
      valueType = ValueType.VALUE;
  }
}