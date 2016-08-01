package com.bytezone.diskbrowser.visicalc;

class Count extends RangeFunction
{
  public Count (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public Value calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

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

      if (cell.getValue () != 0.0)
        value++;
    }

    return this;
  }
}