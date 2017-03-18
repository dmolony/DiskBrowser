package com.bytezone.diskbrowser.visicalc;

public class Sin extends Function
{
  Value v;

  Sin (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@SIN(") : text;

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

    value = Math.sin (v.getValue ());
  }
}