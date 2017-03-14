package com.bytezone.diskbrowser.visicalc;

class Error extends Function
{
  public Error (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);
    valueType = ValueType.ERROR;
  }
}