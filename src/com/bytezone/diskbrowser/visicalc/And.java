package com.bytezone.diskbrowser.visicalc;

class And extends Function
{

  public And (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public void calculate ()
  {
    value = 0;
  }
}