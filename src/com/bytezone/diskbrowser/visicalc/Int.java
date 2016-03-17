package com.bytezone.diskbrowser.visicalc;

public class Int extends Function
{

  Int (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public Value calculate ()
  {
    Expression exp = new Expression (parent, functionText);
    value = (int) exp.getValue ();
    return this;
  }
}