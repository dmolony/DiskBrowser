package com.bytezone.diskbrowser.visicalc;

class IsError extends Function
{
  Cell cell;

  public IsError (Sheet parent, String text)
  {
    super (parent, text);
  }

  @Override
  public Value calculate ()
  {
    if (cell == null)
      cell = parent.getCell (functionText);

    value = cell == null ? 1 : 0;
    valueType = ValueType.VALUE;

    return this;
  }
}