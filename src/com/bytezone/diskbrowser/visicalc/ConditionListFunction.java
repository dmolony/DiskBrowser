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
    String line = "+-------------------------------------------------------------+";
    StringBuilder text = new StringBuilder ();
    text.append (line + "\n");
    text.append (String.format ("| %-10.10s: CFN : %-34.34s%-8.8s|%n",
        cell.getAddressText (), getFullText (), valueType));
    for (Value value : conditions)
      text.append (String.format ("| %-10.10s: %-40.40s%-8.8s|%n", "Value",
          value.getFullText (), value.getValueType ()));
    text.append (line);
    return text.toString ();
  }
}