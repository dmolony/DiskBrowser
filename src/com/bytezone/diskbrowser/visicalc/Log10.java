package com.bytezone.diskbrowser.visicalc;

public class Log10 extends Function
{
  Value v;

  Log10 (Cell cell, String text)
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

    value = Math.log10 (v.getValue ());
  }
}