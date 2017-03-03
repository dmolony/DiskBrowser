package com.bytezone.diskbrowser.visicalc;

public class IsNa extends Function
{
  Value expression;

  IsNa (Sheet parent, String text)
  {
    super (parent, text);

    expression = new Expression (parent, functionText).reduce ();
  }

  @Override
  public void calculate ()
  {
    //    if (expression == null)
    //      expression = new Expression (parent, functionText);

    expression.calculate ();
    value = expression.getValue ();
    valueType = expression.getValueType ();
  }
}