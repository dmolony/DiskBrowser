package com.bytezone.diskbrowser.visicalc;

class And extends Function
{

  public And (Sheet parent, String text)
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