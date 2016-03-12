package com.bytezone.diskbrowser.visicalc;

class IsError extends Function
{
  boolean firstTime = true;
  Cell cell;

  public IsError (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public double getValue ()
  {
    if (firstTime)
    {
      firstTime = false;
      cell = parent.getCell (new Address (functionText));
    }
    return cell == null ? 1 : 0;
  }
}