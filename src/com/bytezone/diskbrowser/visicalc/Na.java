package com.bytezone.diskbrowser.visicalc;

// -----------------------------------------------------------------------------------//
class Na extends ConstantFunction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  Na (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);

    assert text.equals ("@NA") : text;

    valueResult = ValueResult.NA;
    valueType = ValueType.NUMBER;
  }
}