package com.bytezone.diskbrowser.visicalc;

// -----------------------------------------------------------------------------------//
class BooleanFunction extends Function
// -----------------------------------------------------------------------------------//
{
  protected Value source;

  // ---------------------------------------------------------------------------------//
  BooleanFunction (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);

    source = cell.getExpressionValue (functionText);
    values.add (source);
    valueType = ValueType.BOOLEAN;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getType ()
  // ---------------------------------------------------------------------------------//
  {
    return "BooleanFunction";
  }
}