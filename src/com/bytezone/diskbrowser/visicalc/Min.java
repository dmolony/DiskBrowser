package com.bytezone.diskbrowser.visicalc;

class Min extends Function
{
  private final ExpressionList list;

  public Min (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    System.out.println (text);
    list = new ExpressionList (parent, cell, functionText);
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
        break;
      }

      double temp = v.getValue ();
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