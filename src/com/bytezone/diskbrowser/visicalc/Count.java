package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

class Count extends Function
{
  private final ValueList list;
  private final boolean isRange;

  public Count (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@COUNT(") : text;

    list = new ValueList (cell, functionText);
    isRange = functionText.indexOf ("...") > 0;

    for (Value v : list)
      values.add (v);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

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