package com.bytezone.diskbrowser.visicalc;

public class Sqrt extends Function
{
  Sqrt (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@SQRT(") : text;

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

    value = Math.sqrt (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}