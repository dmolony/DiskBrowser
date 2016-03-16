package com.bytezone.diskbrowser.visicalc;

public class IsNa extends Function
{
  Expression expression;

  IsNa (Sheet parent, String text)
  {
    super (parent, text);
    expression = new Expression (parent, functionText);
  }

  @Override
  public void calculate ()
  {
    value = expression.getValue ();
    isError = expression.isError ();
  }
}