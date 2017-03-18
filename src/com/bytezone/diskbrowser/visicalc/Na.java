package com.bytezone.diskbrowser.visicalc;

public class Na extends Function
{
  public Na (Cell cell, String text)
  {
    super (cell, text);
    valueType = ValueType.NA;
  }
}