package com.bytezone.diskbrowser.visicalc;

public class False extends Function
{
  False (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

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