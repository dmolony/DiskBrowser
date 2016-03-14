package com.bytezone.diskbrowser.visicalc;

class Pi extends Function
{
  Pi (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public double getValue ()
  {
    return 3.14159265;
  }
}
