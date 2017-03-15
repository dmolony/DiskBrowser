package com.bytezone.diskbrowser.visicalc;

public class Average extends Function
{
  //  private final Range range;
  private final ExpressionList list;

  public Average (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    //    range = new Range (parent, cell, text);           // use list instead
    list = new ExpressionList (parent, cell, functionText);
  }

  @Override
  public void calculate ()
  {
    double total = 0.0;
    int totalChecked = 0;

    for (Value v : list)
    {
      v.calculate ();
      //      Cell cell = parent.getCell (address);

      if (v.isValueType (ValueType.NA))
        continue;

      if (!v.isValueType (ValueType.VALUE))
      {
        valueType = v.getValueType ();
        return;
      }

      total += v.getValue ();
      totalChecked++;
    }

    if (totalChecked == 0)
    {
      valueType = ValueType.ERROR;
      return;
    }

    value = total / totalChecked;
    valueType = ValueType.VALUE;
  }
}