package com.bytezone.diskbrowser.visicalc;

public class Sqrt extends ValueFunction
{
  Sqrt (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@SQRT(") : text;
  }

  @Override
  public void setValue ()
  {
    value = Math.sqrt (source.getValue ());
  }
}