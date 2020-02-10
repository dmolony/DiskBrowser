package com.bytezone.diskbrowser.visicalc;

// -----------------------------------------------------------------------------------//
abstract class ValueListFunction extends Function
// -----------------------------------------------------------------------------------//
{
  protected final ValueList list;
  protected final boolean isRange;

  // ---------------------------------------------------------------------------------//
  ValueListFunction (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);

    list = new ValueList (cell, functionText);
    isRange = functionText.indexOf ("...") > 0;
    valueType = ValueType.NUMBER;

    for (Value v : list)
      values.add (v);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getType ()
  // ---------------------------------------------------------------------------------//
  {
    return "ValueListFunction";
  }
}