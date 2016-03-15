package com.bytezone.diskbrowser.visicalc;

public class Na extends Function
{
  public Na (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public boolean hasValue ()
  {
    return false;
  }

  @Override
  public String getError ()
  {
    return "@NA";
  }

  @Override
  public double getValue ()
  {
    return 0;
  }
}