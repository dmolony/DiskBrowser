package com.bytezone.diskbrowser.visicalc;

class Count extends Function
{
  private final ExpressionList list;
  private final boolean isRange;

  public Count (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    list = new ExpressionList (parent, cell, functionText);
    isRange = functionText.indexOf ("...") > 0;
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    if (!isRange)
      value = list.size ();
    else
      for (Value v : list)
      {
        v.calculate ();

        if (v instanceof Cell && v.isValueType (ValueType.NA))
          continue;

        if (!v.isValueType (ValueType.VALUE))
        {
          valueType = v.getValueType ();
          return;
        }

        value++;
      }
  }
}