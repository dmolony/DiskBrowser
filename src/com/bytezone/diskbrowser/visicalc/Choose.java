package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Choose extends Function
{
  Choose (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@CHOOSE(") : text;

    // parameters are a Value, followed by a Range
    list = new ValueList (cell, functionText);

    for (Value v : list)
      values.add (v);
  }

  @Override
  public void calculate ()
  {
    Value source = list.get (0);

    source.calculate ();
    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

    int index = (int) source.getValue ();
    if (index < 1 || index >= list.size ())
    {
      valueType = ValueType.NA;
      return;
    }

    Cell cell = (Cell) list.get (index);
    if (cell.isCellType (CellType.EMPTY))
      valueType = ValueType.NA;
    else
    {
      valueType = cell.getValueType ();
      value = cell.getValue ();
    }
  }
}