package com.bytezone.diskbrowser.visicalc;

public class Na extends Function
{
  public Na (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);
    valueType = ValueType.NA;
  }
}