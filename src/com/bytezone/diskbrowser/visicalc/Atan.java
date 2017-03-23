package com.bytezone.diskbrowser.visicalc;

public class Atan extends ValueFunction
{
  Atan (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@ATAN(") : text;
  }

  @Override
  public double calculateValue ()
  {
    return Math.atan (source.getDouble ());
  }
}