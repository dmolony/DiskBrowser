package com.bytezone.diskbrowser.visicalc;

class Max extends RangeFunction
{
  public Max (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public Value calculate ()
  {
    value = Double.MIN_VALUE;
    int totalChecked = 0;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null || cell.isNotAvailable ())
        continue;

      if (cell.isError ())
      {
        valueType = ValueType.ERROR;
        break;
      }

      double temp = cell.getValue ();
      if (temp > value)
        value = temp;
      totalChecked++;
    }

    if (totalChecked == 0)
      valueType = ValueType.NA;
    else
      valueType = ValueType.VALUE;

    return this;
  }
}