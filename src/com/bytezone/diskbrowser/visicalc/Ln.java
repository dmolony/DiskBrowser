package com.bytezone.diskbrowser.visicalc;

public class Ln extends Function
{
  Value v;

  Ln (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@LN(") : text;

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

    value = Math.log (v.getValue ());
  }
}