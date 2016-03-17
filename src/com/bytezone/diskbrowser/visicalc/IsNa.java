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
  public Value calculate ()
  {
    value = expression.getValue ();
    valueType = expression.getValueType ();
    return this;
  }
}