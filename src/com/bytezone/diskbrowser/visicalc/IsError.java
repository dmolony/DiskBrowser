package com.bytezone.diskbrowser.visicalc;

class IsError extends Function
{
  Value expression;

  public IsError (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ISERROR(") : text;

    expression = new Expression (parent, cell, functionText).reduce ();
  }

  @Override
  public void calculate ()
  {
    expression.calculate ();
    value = expression.isValueType (ValueType.ERROR) ? 1 : 0;
    valueType = ValueType.VALUE;
  }
}