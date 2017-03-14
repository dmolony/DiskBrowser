package com.bytezone.diskbrowser.visicalc;

public class Abs extends Function
{
  private final Expression source;

  Abs (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    source = new Expression (parent, cell, functionText);
    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();

    value = Math.abs (source.getValue ());
    valueType = source.getValueType ();
  }
}