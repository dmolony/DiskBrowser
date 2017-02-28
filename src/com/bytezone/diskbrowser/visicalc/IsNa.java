package com.bytezone.diskbrowser.visicalc;

public class IsNa extends Function
{
  Expression expression;

  IsNa (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public void calculate ()
  {
    if (expression == null)
      expression = new Expression (parent, functionText);

    expression.calculate ();
    value = expression.getValue ();
    valueType = expression.getValueType ();
  }
}