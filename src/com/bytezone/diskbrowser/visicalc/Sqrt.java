package com.bytezone.diskbrowser.visicalc;

public class Sqrt extends Function
{
  private final Expression source;

  Sqrt (Cell cell, String text)
  {
    super (cell, text);
    source = new Expression (parent, cell, text.substring (5, text.length () - 1));
    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();

    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

    value = Math.sqrt (source.getValue ());
    valueType = ValueType.VALUE;
  }
}