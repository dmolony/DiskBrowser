package com.bytezone.diskbrowser.visicalc;

public class Abs extends Function
{
  private boolean hasChecked;
  private double value = 0;

  Abs (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public boolean hasValue ()
  {
    if (!hasChecked)
      calculate ();
    return hasValue;
  }

  @Override
  public double getValue ()
  {
    return hasValue () ? value : 0;
  }

  private void calculate ()
  {
    hasChecked = true;
    hasValue = true;

    Expression exp = new Expression (parent, functionText);
    value = Math.abs (exp.getValue ());
  }
}