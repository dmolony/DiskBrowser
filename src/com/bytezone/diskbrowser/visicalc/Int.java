package com.bytezone.diskbrowser.visicalc;

public class Int extends ValueFunction
{
  Int (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@INT(") : text;
  }

  @Override
  public void setValue ()
  {
    value = (int) source.getValue ();
  }
}