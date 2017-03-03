package com.bytezone.diskbrowser.visicalc;

public class Abs extends Function
{
  private final Expression source;

  Abs (Sheet parent, String text)
  {
    super (parent, text);

    source = new Expression (parent, functionText);
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