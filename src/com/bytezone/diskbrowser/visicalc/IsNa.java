package com.bytezone.diskbrowser.visicalc;

public class IsNa extends ValueFunction
{
  IsNa (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@ISNA(") : text;
  }

  @Override
  public boolean isBoolean ()
  {
    return true;
  }

  @Override
  public void setValue ()
  {
    value = source.isValueType (ValueType.NA) ? 1 : 0;
  }
}