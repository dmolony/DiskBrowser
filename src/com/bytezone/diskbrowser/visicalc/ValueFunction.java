package com.bytezone.diskbrowser.visicalc;

public abstract class ValueFunction extends Function
{
  protected Value source;

  ValueFunction (Cell cell, String text)
  {
    super (cell, text);

    source = cell.getExpressionValue (functionText);
    values.add (source);
    // is valueType NUMBER?
  }

  @Override
  public void calculate ()
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

  abstract double calculateValue ();

  @Override
  public String getType ()
  {
    return "ValueFunction";
  }
}