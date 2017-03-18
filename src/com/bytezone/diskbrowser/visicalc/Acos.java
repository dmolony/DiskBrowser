package com.bytezone.diskbrowser.visicalc;

public class Acos extends Function
{
  private final Value source;

  Acos (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ACOS(") : text;

    source = new Expression (parent, cell, functionText).reduce ();
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

    value = Math.acos (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}