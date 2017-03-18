package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Npv extends Function
{
  private final String valueText;
  private final String rangeText;

  private final Expression rateExp;
  private final Range range;

  Npv (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@NPV(") : text;

    valueText = Expression.getParameter (functionText);
    rateExp = new Expression (parent, cell, valueText);
    values.add (rateExp);

    rangeText = functionText.substring (valueText.length () + 1);
    range = new Range (parent, cell, rangeText);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    rateExp.calculate ();
    if (!rateExp.isValueType (ValueType.VALUE))
    {
      valueType = rateExp.getValueType ();
      return;
    }

    double rate = 1 + rateExp.getValue ();

    int period = 0;
    for (Address address : range)
    {
      ++period;

      Cell cell = parent.getCell (address);
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