package com.bytezone.diskbrowser.visicalc;

class Pi extends ConstantFunction
{
  Pi (Cell cell, String text)
  {
    super (cell, text);

    assert text.equals ("@PI") : text;

    value = Math.PI;
    valueType = ValueType.VALUE;
  }
}