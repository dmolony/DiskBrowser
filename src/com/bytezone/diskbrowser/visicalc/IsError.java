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
  public void setValue ()
  {
    value = source.isValueType (ValueType.ERROR) ? 1 : 0;
  }
}