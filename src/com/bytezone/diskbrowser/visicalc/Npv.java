package com.bytezone.diskbrowser.visicalc;

public class Npv extends Function
{
  private final String valueText;
  private final String rangeText;

  private final Expression rateExp;
  private final Range range;

  Npv (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    int pos = text.indexOf (',');
    valueText = text.substring (5, pos);
    rangeText = text.substring (pos + 1, text.length () - 1);

    rateExp = new Expression (parent, cell, valueText);
    range = new Range (parent, rangeText);

    values.add (rateExp);
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
      if (cell.isValueType (ValueType.NA))
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