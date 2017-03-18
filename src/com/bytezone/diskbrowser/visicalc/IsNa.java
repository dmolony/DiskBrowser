package com.bytezone.diskbrowser.visicalc;

public class IsNa extends Function
{
  Value expression;

  IsNa (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ISNA(") : text;

    expression = new Expression (parent, cell, functionText).reduce ();
  }

  @Override
  public void calculate ()
  {
    expression.calculate ();
    value = expression.isValueType (ValueType.NA) ? 1 : 0;
    valueType = expression.getValueType ();
  }
}