package com.bytezone.diskbrowser.visicalc;

// -----------------------------------------------------------------------------------//
class Not extends BooleanFunction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  Not (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);
    assert text.startsWith ("@NOT(") : text;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void calculate ()
  // ---------------------------------------------------------------------------------//
  {
    source.calculate ();

    if (source.getValueType () != ValueType.BOOLEAN)
    {
      valueResult = ValueResult.ERROR;
      return;
    }

    bool = !source.getBoolean ();
    valueResult = ValueResult.VALID;
  }
}