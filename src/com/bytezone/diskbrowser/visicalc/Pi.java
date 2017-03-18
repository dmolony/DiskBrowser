package com.bytezone.diskbrowser.visicalc;

class Pi extends Function
{
  Pi (Cell cell, String text)
  {
    super (cell, text);

    value = Math.PI;

    assert text.equals ("@PI") : text;

    valueType = ValueType.VALUE;
  }
}