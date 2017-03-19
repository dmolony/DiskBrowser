package com.bytezone.diskbrowser.visicalc;

public class Exp extends Function
{
  Exp (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@EXP(") : text;

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

    value = Math.exp (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}