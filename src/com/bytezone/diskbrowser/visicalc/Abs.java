package com.bytezone.diskbrowser.visicalc;

public class Abs extends Function
{
  Abs (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ABS(") : text;

    source = cell.getExpressionValue (functionText);
    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();

    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

    value = Math.abs (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}