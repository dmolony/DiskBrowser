package com.bytezone.diskbrowser.visicalc;

public class BooleanFunction extends Function
{
  protected Value source;

  BooleanFunction (Cell cell, String text)
  {
    super (cell, text);

    source = cell.getExpressionValue (functionText);
    values.add (source);
    valueType = ValueType.BOOLEAN;
  }

  @Override
  public boolean getBoolean ()
  {
    return bool;
  }

  @Override
  public String getType ()
  {
    return "BooleanFunction";
  }
}