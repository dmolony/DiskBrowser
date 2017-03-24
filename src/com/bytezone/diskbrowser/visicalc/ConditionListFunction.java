package com.bytezone.diskbrowser.visicalc;

public class ConditionListFunction extends Function
{
  protected final ConditionList conditions;

  ConditionListFunction (Cell cell, String text)
  {
    super (cell, text);

    conditions = new ConditionList (cell, functionText);
    valueType = ValueType.BOOLEAN;
  }

  @Override
  public String getType ()
  {
    return "ConditionListFunction";
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("%s%n", LINE));
    text.append (
        String.format (FMT4, "CFN", getFullText (), valueType, getValueText (this)));
    for (Value value : conditions)
      text.append (String.format (FMT4, value.getType (), value.getFullText (),
          value.getValueType (), getValueText (value)));
    return text.toString ();
  }
}