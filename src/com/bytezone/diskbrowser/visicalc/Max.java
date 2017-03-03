package com.bytezone.diskbrowser.visicalc;

class Max extends Function
{
  private final Range range;

  public Max (Sheet parent, String text)
  {
    super (parent, text);
    range = new Range (parent, text);
  }

  @Override
  public void calculate ()
  {
    value = Double.MIN_VALUE;
    int totalChecked = 0;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null || cell.isValueType (ValueType.NA))
        continue;

      if (!cell.isValueType (ValueType.VALUE))
      {
        valueType = cell.getValueType ();
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
  }
}