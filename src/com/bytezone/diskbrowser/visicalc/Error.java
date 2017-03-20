package com.bytezone.diskbrowser.visicalc;

class Error extends ConstantFunction
{
  public Error (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ERROR") : text;

    valueType = ValueType.ERROR;
  }
}