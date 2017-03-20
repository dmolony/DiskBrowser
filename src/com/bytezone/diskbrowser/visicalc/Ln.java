package com.bytezone.diskbrowser.visicalc;

public class Ln extends ValueFunction
{
  Ln (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@LN(") : text;
  }

  @Override
  public double calculateValue ()
  {
    return Math.log (source.getValue ());
  }
}