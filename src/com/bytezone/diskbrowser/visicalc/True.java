package com.bytezone.diskbrowser.visicalc;

public class True extends ConstantFunction
{
  True (Cell cell, String text)
  {
    super (cell, text);

    assert text.equals ("@TRUE") : text;

    value = 1;
    valueType = ValueType.VALUE;
  }

  @Override
  public boolean isBoolean ()
  {
    return true;
  }

  @Override
  public String getText ()
  {
    return value == 0 ? "FALSE" : "TRUE";
  }
}