package com.bytezone.diskbrowser.visicalc;

public class Int extends Function
{
  Int (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@INT(") : text;

    source = cell.getExpressionValue (functionText);
    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();

    value = (int) source.getValue ();
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }
}