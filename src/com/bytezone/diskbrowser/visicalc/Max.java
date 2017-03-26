package com.bytezone.diskbrowser.visicalc;

class Max extends ValueListFunction
{
  public Max (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@MAX(") : text;
  }

  @Override
  public void calculate ()
  {
    value = Double.MIN_VALUE;
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

      value = Math.max (value, v.getDouble ());
      totalChecked++;
    }

    if (totalChecked == 0)
      valueResult = ValueResult.NA;
  }
}