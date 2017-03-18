package com.bytezone.diskbrowser.visicalc;

public class False extends Function
{
  False (Cell cell, String text)
  {
    super (cell, text);

    assert text.equals ("@FALSE") : text;

    value = 0;
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