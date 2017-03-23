package com.bytezone.diskbrowser.visicalc;

public class True extends ConstantFunction
{
  True (Cell cell, String text)
  {
    super (cell, text);

    assert text.equals ("@TRUE") : text;

    bool = true;
    valueType = ValueType.BOOLEAN;
  }
}