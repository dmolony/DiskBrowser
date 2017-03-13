package com.bytezone.diskbrowser.visicalc;

class IsError extends Function
{
  Value expression;

  public IsError (Sheet parent, String text)
  {
    super (parent, text);

    expression = new Expression (parent, functionText).reduce ();
  }

  @Override
  public void calculate ()
  {
    expression.calculate ();
    value = expression.isValueType (ValueType.ERROR) ? 1 : 0;
    valueType = ValueType.VALUE;
  }
}