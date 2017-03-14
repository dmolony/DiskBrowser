package com.bytezone.diskbrowser.visicalc;

public class Int extends Function
{
  Expression source;

  Int (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    source = new Expression (parent, cell, functionText);
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