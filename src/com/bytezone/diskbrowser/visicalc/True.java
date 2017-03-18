package com.bytezone.diskbrowser.visicalc;

public class True extends Function
{
  True (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

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