package com.bytezone.diskbrowser.visicalc;

public class Sin extends ValueFunction
{
  Sin (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@SIN(") : text;
  }

  @Override
  public double calculateValue ()
  {
    return Math.sin (source.getDouble ());
  }
}