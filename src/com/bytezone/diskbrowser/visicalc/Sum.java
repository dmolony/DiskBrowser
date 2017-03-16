package com.bytezone.diskbrowser.visicalc;

class Sum extends Function
{
  private final ValueList list;

  public Sum (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    list = new ValueList (parent, cell, functionText);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    for (Value v : list)
    {
      v.calculate ();

      if (v.isValueType (ValueType.NA))
        continue;

      if (!v.isValueType (ValueType.VALUE))
      {
        valueType = v.getValueType ();
        break;
      }

      value += v.getValue ();
    }
  }
}