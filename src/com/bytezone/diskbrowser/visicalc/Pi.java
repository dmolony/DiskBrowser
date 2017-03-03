package com.bytezone.diskbrowser.visicalc;

class Pi extends Function
{
  Pi (Sheet parent, String text)
  {
    super (parent, text);

    value = Math.PI;
    valueType = ValueType.VALUE;
  }
}