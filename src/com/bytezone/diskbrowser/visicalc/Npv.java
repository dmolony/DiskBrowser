package com.bytezone.diskbrowser.visicalc;

public class Npv extends Function
{
  //  private final String valueText;
  //  private final String rangeText;
  //
  //  private final Expression valueExp;
  private final Range range;

  Npv (Sheet parent, String text)
  {
    super (parent, text);
    range = new Range (text);

    //    int pos = text.indexOf (',');
    //    valueText = text.substring (8, pos);
    //    rangeText = text.substring (pos + 1, text.length () - 1);
    //
    //    valueExp = new Expression (parent, valueText);
  }

  @Override
  public Value calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null || cell.isValueType (ValueType.NA))
        continue;

      if (cell.isValueType (ValueType.ERROR))
      {
        valueType = ValueType.ERROR;
        break;
      }

      double temp = cell.getValue ();
    }

    return this;
  }
}