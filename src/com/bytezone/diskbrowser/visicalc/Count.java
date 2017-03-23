package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

class Count extends ValueListFunction
{
  public Count (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@COUNT(") : text;
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.NUMBER;

    if (!isRange)
      value = list.size ();
    else
      for (Value v : list)
      {
        if (v instanceof Cell && ((Cell) v).isCellType (CellType.EMPTY))
          continue;

        v.calculate ();     // is this required?
        value++;
      }
  }
}