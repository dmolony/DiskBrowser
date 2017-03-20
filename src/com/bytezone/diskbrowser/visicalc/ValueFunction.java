package com.bytezone.diskbrowser.visicalc;

public abstract class ValueFunction extends Function
{
  protected Value source;

  ValueFunction (Cell cell, String text)
  {
    super (cell, text);

    source = cell.getExpressionValue (functionText);
    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();

    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

    value = calculateValue ();
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }

  abstract double calculateValue ();
}