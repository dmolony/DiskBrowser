package com.bytezone.diskbrowser.visicalc;

class Sum extends Function
{
  private final Range range;

  public Sum (Sheet parent, String text)
  {
    super (parent, text);

    range = new Range (parent, text);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

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

      value += cell.getValue ();
    }
  }
}