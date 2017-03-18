package com.bytezone.diskbrowser.visicalc;

public class Ln extends Function
{
  private final Value source;

  Ln (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@LN(") : text;

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

    value = Math.log (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}