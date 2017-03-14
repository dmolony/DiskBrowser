package com.bytezone.diskbrowser.visicalc;

class Pi extends Function
{
  Pi (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    value = Math.PI;
    valueType = ValueType.VALUE;
  }
}