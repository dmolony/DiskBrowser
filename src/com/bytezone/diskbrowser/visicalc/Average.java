package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Average extends Function
{
  private final ValueList list;
  private final boolean isRange;      // may affect how the count is done

  public Average (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    list = new ValueList (parent, cell, functionText);
    isRange = functionText.indexOf ("...") > 0;
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