package com.bytezone.diskbrowser.visicalc;

class Error extends Function
{
  public Error (Cell cell, String text)
  {
    super (cell, text);
    valueType = ValueType.ERROR;
  }
}