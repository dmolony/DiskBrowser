package com.bytezone.diskbrowser.visicalc;

public class Na extends Function
{
  public Na (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public boolean isError ()
  {
    return false;
  }

  @Override
  public boolean isNotAvailable ()
  {
    return true;
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