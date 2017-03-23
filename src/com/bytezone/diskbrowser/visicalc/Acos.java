package com.bytezone.diskbrowser.visicalc;

public class Acos extends ValueFunction
{
  Acos (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@ACOS(") : text;
  }

  @Override
  public double calculateValue ()
  {
    return Math.acos (source.getDouble ());
  }
}