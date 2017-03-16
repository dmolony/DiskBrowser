package com.bytezone.diskbrowser.visicalc;

public class Sin extends Function
{
  Value v;

  Sin (Sheet parent, Cell cell, String text)
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

    value = Math.sin (v.getValue ());
  }
}