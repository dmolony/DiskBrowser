package com.bytezone.diskbrowser.visicalc;

class Min extends Function
{
  private final Range range;

  public Min (Sheet parent, String text)
  {
    super (parent, text);

    range = new Range (parent, text);
  }

  @Override
  public void calculate ()
  {
    value = Double.MAX_VALUE;
    int totalChecked = 0;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell.isValueType (ValueType.NA))
        continue;

      if (!cell.isValueType (ValueType.VALUE))
      {
        valueType = cell.getValueType ();
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
  }
}