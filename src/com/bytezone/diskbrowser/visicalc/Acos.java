package com.bytezone.diskbrowser.visicalc;

public class Acos extends Function
{
  Value v;

  Acos (Cell cell, String text)
  {
    super (cell, text);

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

    value = Math.acos (v.getValue ());

    if (Double.isNaN (value))
      valueType = ValueType.ERROR;
  }
}