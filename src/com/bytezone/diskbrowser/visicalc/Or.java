package com.bytezone.diskbrowser.visicalc;

class Or extends Function
{
  public Or (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public Value calculate ()
  {
    value = 0;
    return this;
  }
}