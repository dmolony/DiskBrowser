package com.bytezone.diskbrowser.visicalc;

class Min extends Function
{
  public Min (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@MIN(") : text;

    list = new ValueList (cell, functionText);

    for (Value v : list)
      values.add (v);
  }

  @Override
  public void calculate ()
  {
    value = Double.MAX_VALUE;
    int totalChecked = 0;

    for (Value v : list)
    {
      v.calculate ();
      if (!v.isValueType (ValueType.VALUE))
      {
        valueType = cell.getValueType ();
        return;
      }

      value = Math.min (value, v.getValue ());
      totalChecked++;
    }

    valueType = totalChecked == 0 ? ValueType.NA : ValueType.VALUE;
  }
}