package com.bytezone.diskbrowser.visicalc;

public class Log10 extends ValueFunction
{
  Log10 (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@LOG10(") : text;
  }

  @Override
  public void setValue ()
  {
    value = Math.log10 (source.getValue ());
  }
}