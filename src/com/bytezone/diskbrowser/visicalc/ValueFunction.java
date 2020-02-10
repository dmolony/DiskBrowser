package com.bytezone.diskbrowser.visicalc;

// -----------------------------------------------------------------------------------//
abstract class ValueFunction extends Function
// -----------------------------------------------------------------------------------//
{
  protected Value source;

  abstract double calculateValue ();

  // ---------------------------------------------------------------------------------//
  ValueFunction (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);

    source = cell.getExpressionValue (functionText);
    values.add (source);
    valueType = ValueType.NUMBER;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void calculate ()
  // ---------------------------------------------------------------------------------//
  {
    valueResult = ValueResult.VALID;

    source.calculate ();

    if (!source.isValid ())
    {
      valueResult = source.getValueResult ();
      return;
    }

    value = calculateValue ();

    if (Double.isNaN (value))
      valueResult = ValueResult.ERROR;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getType ()
  // ---------------------------------------------------------------------------------//
  {
    return "ValueFunction";
  }
}