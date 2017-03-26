package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Choose extends ValueListFunction
{
  Choose (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@CHOOSE(") : text;
  }

  @Override
  public void calculate ()
  {
    Value source = list.get (0);
    valueResult = ValueResult.VALID;

    source.calculate ();
    if (!source.isValid ())
    {
      valueResult = source.getValueResult ();
      return;
    }

    int index = (int) source.getDouble ();
    if (index < 1 || index >= list.size ())
    {
      valueResult = ValueResult.NA;
      return;
    }

    Cell cell = (Cell) list.get (index);
    if (cell.isCellType (CellType.EMPTY))
      valueResult = ValueResult.NA;
    else
    {
      valueResult = cell.getValueResult ();
      value = cell.getDouble ();
    }
  }
}