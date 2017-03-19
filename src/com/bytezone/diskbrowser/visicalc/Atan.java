package com.bytezone.diskbrowser.visicalc;

public class Atan extends Function
{
  Atan (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ATAN(") : text;

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

    value = Math.atan (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}