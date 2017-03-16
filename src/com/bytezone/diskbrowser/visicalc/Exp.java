package com.bytezone.diskbrowser.visicalc;

public class Exp extends Function
{
  Value v;

  Exp (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    v = new Expression (parent, cell, functionText).reduce ();
    valueType = ValueType.VALUE;
  }

  @Override
  public void calculate ()
  {
    v.calculate ();
    if (!v.isValueType (ValueType.VALUE))
    {
      valueType = v.getValueType ();
      return;
    }

    value = Math.exp (v.getValue ());

    if (Double.isNaN (value))
      valueType = ValueType.ERROR;
  }
}