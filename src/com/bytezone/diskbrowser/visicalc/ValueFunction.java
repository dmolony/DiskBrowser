package com.bytezone.diskbrowser.visicalc;

public abstract class ValueFunction extends Function
{
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

    //    value = Math.abs (source.getValue ());
    setValue ();
    valueType = Double.isNaN (value) ? ValueType.ERROR : ValueType.VALUE;
  }

  abstract void setValue ();
}