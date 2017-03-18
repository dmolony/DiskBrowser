package com.bytezone.diskbrowser.visicalc;

class IsError extends Function
{
  private final Value source;

  public IsError (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ISERROR(") : text;

    source = new Expression (parent, cell, functionText).reduce ();
    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();

    value = source.isValueType (ValueType.ERROR) ? 1 : 0;
    valueType = ValueType.VALUE;
  }
}