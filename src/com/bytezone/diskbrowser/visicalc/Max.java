package com.bytezone.diskbrowser.visicalc;

class Max extends Function
{
  private final ValueList list;

  public Max (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@MAX(") : text;

    list = new ValueList (cell, functionText);

    for (Value v : list)
      values.add (v);
  }

  @Override
  public void calculate ()
  {
    value = Double.MIN_VALUE;
    int totalChecked = 0;

    for (Value v : list)
    {
      v.calculate ();
      if (!v.isValueType (ValueType.VALUE))
      {
        valueType = cell.getValueType ();
        return;
      }

      value = Math.max (value, v.getValue ());
      totalChecked++;
    }

    valueType = totalChecked == 0 ? ValueType.NA : ValueType.VALUE;
  }
}