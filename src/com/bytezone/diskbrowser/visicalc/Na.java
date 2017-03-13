package com.bytezone.diskbrowser.visicalc;

public class Na extends Function
{
  public Na (Sheet parent, String text)
  {
    super (parent, text);
    valueType = ValueType.NA;
  }
}