package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Npv extends Function
{
  private final String sourceText;
  private final String rangeText;

  private final Value source;
  private final Range range;

  Npv (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@NPV(") : text;

    sourceText = Expression.getParameter (functionText);
    source = new Expression (parent, cell, sourceText).reduce ();
    values.add (source);

    rangeText = functionText.substring (sourceText.length () + 1);
    range = new Range (parent, cell, rangeText);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    source.calculate ();
    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

    double rate = 1 + source.getValue ();

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