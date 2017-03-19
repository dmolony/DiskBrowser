package com.bytezone.diskbrowser.visicalc;

class And extends Function
{
  private final ConditionList conditions;

  public And (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@AND(") : text;

    conditions = new ConditionList (cell, functionText);
  }

  @Override
  public void calculate ()
  {
    for (Condition condition : conditions)
    {
      condition.calculate ();
      if (condition.getValue () == 0)
      {
        value = 0;
        return;
      }
    }
    value = 1;
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