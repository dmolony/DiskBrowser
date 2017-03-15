package com.bytezone.diskbrowser.visicalc;

class Max extends Function
{
  private final ExpressionList list;

  public Max (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    list = new ExpressionList (parent, cell, functionText);
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