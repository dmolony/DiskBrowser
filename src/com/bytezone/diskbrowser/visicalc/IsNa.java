package com.bytezone.diskbrowser.visicalc;

public class IsNa extends Function
{
  IsNa (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ISNA(") : text;

    source = cell.getExpressionValue (functionText);
    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();

    value = source.isValueType (ValueType.NA) ? 1 : 0;
    valueType = source.getValueType ();
  }
}