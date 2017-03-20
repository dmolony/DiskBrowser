package com.bytezone.diskbrowser.visicalc;

public class Cos extends ValueFunction
{
  Cos (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@COS(") : text;
  }

  @Override
  public double calculateValue ()
  {
    return Math.cos (source.getValue ());
  }
}