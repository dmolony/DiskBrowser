package com.bytezone.diskbrowser.visicalc;

class Error extends Function
{
  public Error (Sheet parent, String text)
  {
    super (parent, text);
    valueType = ValueType.ERROR;
  }

  @Override
  public double getValue ()
  {
    return 0;
  }

  @Override
  public Value calculate ()
  {
    return this;
  }
}