package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Average extends ValueListFunction
{
  public Average (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@AVERAGE(") : text;
    valueType = ValueType.NUMBER;
  }

  @Override
  public void calculate ()
  {
    double total = 0.0;
    int totalChecked = 0;
    valueResult = ValueResult.VALID;

    for (Value v : list)
    {
      if (v instanceof Cell && ((Cell) v).isCellType (CellType.EMPTY))
        continue;

      v.calculate ();

      if (v.getValueResult () == ValueResult.NA)
        continue;

      if (!v.isValid ())
      {
        valueResult = v.getValueResult ();
        return;
      }

      total += v.getDouble ();
      totalChecked++;
    }

    if (totalChecked == 0)
    {
      valueResult = ValueResult.ERROR;
      return;
    }

    value = total / totalChecked;
  }
}