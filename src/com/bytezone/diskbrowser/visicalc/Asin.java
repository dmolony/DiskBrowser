package com.bytezone.diskbrowser.visicalc;

public class Asin extends Function
{
  Value v;

  Asin (Cell cell, String text)
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

    value = Math.asin (v.getValue ());

    if (Double.isNaN (value))
      valueType = ValueType.ERROR;
  }
}