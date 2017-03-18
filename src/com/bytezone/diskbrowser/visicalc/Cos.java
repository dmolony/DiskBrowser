package com.bytezone.diskbrowser.visicalc;

public class Cos extends Function
{
  private final Value source;

  Cos (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@COS(") : text;

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

    value = Math.cos (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}