package com.bytezone.diskbrowser.visicalc;

public class Sin extends Function
{
  private final Value source;

  Sin (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@SIN(") : text;

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

    value = Math.sin (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}