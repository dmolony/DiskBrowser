package com.bytezone.diskbrowser.visicalc;

class IsError extends Function
{
  Expression expression;

  public IsError (Sheet parent, String text)
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