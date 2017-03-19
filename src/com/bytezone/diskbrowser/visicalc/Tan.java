package com.bytezone.diskbrowser.visicalc;

public class Tan extends ValueFunction
{
  Tan (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@TAN(") : text;
  }

  @Override
  public void setValue ()
  {
    value = Math.tan (source.getValue ());
  }
}