package com.bytezone.diskbrowser.visicalc;

class IsError extends Function
{
  Cell cell;

  public IsError (Sheet parent, String text)
  {
    super (parent, text);

    cell = parent.getCell (functionText);
  }

  @Override
  public void calculate ()
  {
    //    if (cell == null)
    //      cell = parent.getCell (functionText);

    value = cell == null ? 1 : cell.isValueType (ValueType.ERROR) ? 1 : 0;
    valueType = ValueType.VALUE;
  }
}