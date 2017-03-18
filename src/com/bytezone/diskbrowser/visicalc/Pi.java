package com.bytezone.diskbrowser.visicalc;

class Pi extends Function
{
  Pi (Cell cell, String text)
  {
    super (cell, text);

    value = Math.PI;
    valueType = ValueType.VALUE;
  }
}