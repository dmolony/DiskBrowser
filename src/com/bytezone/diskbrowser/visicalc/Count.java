package com.bytezone.diskbrowser.visicalc;

class Count extends Function
{
  private final Range range;

  public Count (Sheet parent, String text)
  {
    super (parent, text);
    range = new Range (text);
  }

  @Override
  public Value calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null || cell.isValueType (ValueType.NA))
        continue;

      if (cell.isValueType (ValueType.ERROR))
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