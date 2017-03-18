package com.bytezone.diskbrowser.visicalc;

class Sum extends Function
{
  private final ValueList list;

  public Sum (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@SUM(") : text;

    list = new ValueList (parent, cell, functionText);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    for (Value v : list)
    {
      v.calculate ();

      if (v.isValueType (ValueType.NA))
        continue;

      if (!v.isValueType (ValueType.VALUE))
      {
        valueType = v.getValueType ();
        break;
      }

      value += v.getValue ();
    }
  }
}