package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Average extends ValueListFunction
{
  public Average (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@AVERAGE(") : text;
  }

  @Override
  public void calculate ()
  {
    double total = 0.0;
    int totalChecked = 0;

    for (Value v : list)
    {
      if (v instanceof Cell && ((Cell) v).isCellType (CellType.EMPTY))
        continue;

      v.calculate ();

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