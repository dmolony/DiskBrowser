package com.bytezone.diskbrowser.visicalc;

public class Exp extends ValueFunction
{
  Exp (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@EXP(") : text;
  }

  @Override
  public void setValue ()
  {
    value = Math.exp (source.getValue ());
  }
}