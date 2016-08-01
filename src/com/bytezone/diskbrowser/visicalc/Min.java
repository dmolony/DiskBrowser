package com.bytezone.diskbrowser.visicalc;

class Min extends RangeFunction
{
  public Min (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public Value calculate ()
  {
    value = Double.MAX_VALUE;
    int totalChecked = 0;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null || cell.is (ValueType.NA))
        continue;

      if (cell.is (ValueType.ERROR))
      {
        valueType = ValueType.ERROR;
        break;
      }

      double temp = cell.getValue ();
      if (temp < value)
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