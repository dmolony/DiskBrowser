package com.bytezone.diskbrowser.visicalc;

public class Sqrt extends Function
{
  private final Expression source;

  Sqrt (Sheet parent, String text)
  {
    super (parent, text);
    source = new Expression (parent, text.substring (5, text.length () - 1));
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