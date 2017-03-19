package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Npv extends Function
{
  Npv (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@NPV(") : text;

    // parameters are a Value, followed by a Range
    list = new ValueList (cell, functionText);

    for (Value v : list)
      values.add (v);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    Value source = list.get (0);
    source.calculate ();
    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

    double rate = 1 + source.getValue ();

    int period = 0;
    int pos = 0;
    for (int i = 1; i < list.size (); i++)
    {
      Cell cell = (Cell) list.get (i);

      ++period;

      if (cell.isCellType (CellType.EMPTY))
        continue;

      if (!cell.isValueType (ValueType.VALUE))
      {
        valueType = cell.getValueType ();
        return;
      }

      value += cell.getValue () / Math.pow (rate, period);
    }
  }
}