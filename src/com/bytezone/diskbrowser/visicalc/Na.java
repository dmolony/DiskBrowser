package com.bytezone.diskbrowser.visicalc;

public class Na extends ConstantFunction
{
  public Na (Cell cell, String text)
  {
    super (cell, text);

    assert text.equals ("@NA") : text;

    valueType = ValueType.NA;
  }
}