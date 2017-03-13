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
    expression.calculate ();
    value = expression.isValueType (ValueType.NA) ? 1 : 0;
    valueType = expression.getValueType ();
  }
}