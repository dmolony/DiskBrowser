package com.bytezone.diskbrowser.visicalc;

public class Abs extends Function
{
  private final Expression source;

  Abs (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ABS(") : text;

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