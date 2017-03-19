package com.bytezone.diskbrowser.visicalc;

public class Abs extends ValueFunction
{
  Abs (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@ABS(") : text;
  }

  @Override
  void setValue ()
  {
    value = Math.abs (source.getValue ());
  }
}