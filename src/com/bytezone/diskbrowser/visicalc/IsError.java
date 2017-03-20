package com.bytezone.diskbrowser.visicalc;

class IsError extends ValueFunction
{
  public IsError (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@ISERROR(") : text;
  }

  @Override
  public boolean isBoolean ()
  {
    return true;
  }

  @Override
  public void calculate ()
  {
    source.calculate ();
    value = calculateValue ();
    valueType = ValueType.VALUE;      // do not use source.getValueType()
  }

  @Override
  public double calculateValue ()
  {
    return source.isValueType (ValueType.ERROR) ? 1 : 0;
  }
}