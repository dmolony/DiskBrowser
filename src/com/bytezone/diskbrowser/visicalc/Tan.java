package com.bytezone.diskbrowser.visicalc;

public class Tan extends ValueFunction
{
  Tan (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@TAN(") : text;
  }

  @Override
  public double calculateValue ()
  {
    return Math.tan (source.getValue ());
  }
}