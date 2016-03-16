package com.bytezone.diskbrowser.visicalc;

class Pi extends Function
{
  Pi (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public void calculate ()
  {
    value = 3.1415926536;
  }
}
