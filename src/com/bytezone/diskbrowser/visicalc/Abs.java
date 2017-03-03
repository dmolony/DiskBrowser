package com.bytezone.diskbrowser.visicalc;

public class Abs extends Function
{
  Expression source;

  Abs (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public void calculate ()
  {
    if (source == null)
    {
      source = new Expression (parent, functionText);
      values.add (source);
      source.calculate ();
    }

    value = Math.abs (source.getValue ());
    valueType = source.getValueType ();
  }
}