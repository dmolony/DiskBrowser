package com.bytezone.diskbrowser.visicalc;

public class Int extends Function
{
  Expression source;

  Int (Sheet parent, String text)
  {
    super (parent, text);

    source = new Expression (parent, functionText);
    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();
    value = (int) source.getValue ();
    valueType = source.getValueType ();
  }
}