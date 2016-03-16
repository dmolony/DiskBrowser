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
    expression.calculate ();
    //    value = expression.getValue ();
    valueType = expression.getValueType ();
    value = isError () ? 1 : 0;
  }
}