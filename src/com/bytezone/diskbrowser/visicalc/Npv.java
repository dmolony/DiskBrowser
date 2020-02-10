package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

// -----------------------------------------------------------------------------------//
class Npv extends ValueListFunction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  Npv (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);

    assert text.startsWith ("@NPV(") : text;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void calculate ()
  // ---------------------------------------------------------------------------------//
  {
    value = 0;
    valueResult = ValueResult.VALID;

    Value source = list.get (0);                    // first Value is the rate
    source.calculate ();

    if (!source.isValid ())
    {
      valueResult = source.getValueResult ();
      return;
    }

    double rate = 1 + source.getDouble ();
    int period = 0;

    for (int i = 1; i < list.size (); i++)          // remaining Values are Cells
    {
      ++period;

      Cell cell = (Cell) list.get (i);
      if (cell.isCellType (CellType.EMPTY))
        continue;

      if (!cell.isValid ())
      {
        valueResult = source.getValueResult ();
        return;
      }

      value += cell.getDouble () / Math.pow (rate, period);
    }
  }
}