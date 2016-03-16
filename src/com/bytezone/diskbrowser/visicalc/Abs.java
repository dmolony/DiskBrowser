package com.bytezone.diskbrowser.visicalc;

public class Abs extends Function
{
  Abs (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public void calculate ()
  {
    Expression exp = new Expression (parent, functionText);
    value = Math.abs (exp.getValue ());
  }
}