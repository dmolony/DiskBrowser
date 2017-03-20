package com.bytezone.diskbrowser.visicalc;

class Or extends Function
{
  private final ConditionList conditions;

  public Or (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@OR(") : text;

    conditions = new ConditionList (cell, functionText);
  }

  @Override
  public void calculate ()
  {
    for (Condition condition : conditions)
    {
      condition.calculate ();
      if (condition.getValue () == 1)
      {
        value = 1;
        return;
      }
    }
    value = 0;
  }

  @Override
  public boolean isBoolean ()
  {
    return true;
  }

  @Override
  public String getText ()
  {
    return value == 0 ? "FALSE" : "TRUE";
  }
}