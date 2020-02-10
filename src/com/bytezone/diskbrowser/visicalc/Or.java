package com.bytezone.diskbrowser.visicalc;

// -----------------------------------------------------------------------------------//
class Or extends ConditionListFunction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  Or (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);
    assert text.startsWith ("@OR(") : text;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void calculate ()
  // ---------------------------------------------------------------------------------//
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

      if (condition.getBoolean ())
      {
        bool = true;
        return;
      }
    }

    bool = false;
  }
}