package com.bytezone.diskbrowser.visicalc;

class Min extends ValueListFunction
{
  public Min (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@MIN(") : text;
  }

  @Override
  public void calculate ()
  {
    value = Double.MAX_VALUE;
    int totalChecked = 0;
    valueResult = ValueResult.VALID;

    for (Value v : list)
    {
      v.calculate ();
      if (!v.isValid ())
      {
        valueResult = cell.getValueResult ();
        return;
      }

      value = Math.min (value, v.getDouble ());
      totalChecked++;
    }

    if (totalChecked == 0)
      valueResult = ValueResult.NA;
  }
}