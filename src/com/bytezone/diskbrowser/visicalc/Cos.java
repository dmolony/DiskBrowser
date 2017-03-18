package com.bytezone.diskbrowser.visicalc;

public class Cos extends Function
{
  Value v;

  Cos (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@COS(") : text;

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

    value = Math.cos (v.getValue ());
  }
}