package com.bytezone.diskbrowser.visicalc;

public class ConditionListFunction extends Function
{
  protected final ConditionList conditions;

  ConditionListFunction (Cell cell, String text)
  {
    super (cell, text);

    conditions = new ConditionList (cell, functionText);
    for (Value condition : conditions)
      values.add (condition);

    valueType = ValueType.BOOLEAN;
  }

  @Override
  public String getType ()
  {
    return "CLF";
  }
}