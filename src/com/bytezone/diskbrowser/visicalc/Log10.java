package com.bytezone.diskbrowser.visicalc;

public class Log10 extends Function
{
  private final Value source;

  Log10 (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@LOG10(") : text;

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

    value = Math.log10 (source.getValue ());
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}