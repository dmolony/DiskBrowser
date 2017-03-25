package com.bytezone.diskbrowser.visicalc;

class And extends ConditionListFunction
{
  public And (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@AND(") : text;
  }

  @Override
  public void calculate ()
  {
    valueResult = ValueResult.VALID;

    for (Value condition : conditions)
    {
      condition.calculate ();

      if (condition.getValueType () != ValueType.BOOLEAN)
      {
        valueResult = ValueResult.ERROR;
        return;
      }

      if (!condition.getBoolean ())
      {
        bool = false;
        return;
      }
    }

    bool = true;
  }
}