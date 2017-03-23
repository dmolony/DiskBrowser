package com.bytezone.diskbrowser.visicalc;

public class Abs extends ValueFunction
{
  Abs (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@ABS(") : text;
  }

  @Override
  double calculateValue ()
  {
    return Math.abs (source.getDouble ());
  }
}