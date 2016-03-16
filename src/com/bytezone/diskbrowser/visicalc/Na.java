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
    return true;
  }

  @Override
  public boolean isNaN ()
  {
    return true;
  }

  @Override
  public double getValue ()
  {
    return 0;
  }

  @Override
  public void calculate ()
  {
  }
}