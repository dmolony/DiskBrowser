package com.bytezone.diskbrowser.visicalc;

class Min extends Function
{
  private final ValueList list;

  public Min (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    list = new ValueList (parent, cell, functionText);
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