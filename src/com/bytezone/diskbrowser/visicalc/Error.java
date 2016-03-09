package com.bytezone.diskbrowser.visicalc;

public class Error extends Function
{
  public Error (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public double getValue ()
  {
    return 0;
  }
}