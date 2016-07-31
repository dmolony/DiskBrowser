package com.bytezone.diskbrowser.visicalc;

public class Npv extends RangeFunction
{
  //  private final String valueText;
  //  private final String rangeText;
  //
  //  private final Expression valueExp;

  Npv (Sheet parent, String text)
  {
    super (parent, text);

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
      if (cell == null || cell.isNotAvailable ())
        continue;

      if (cell.isError ())
      {
        valueType = ValueType.ERROR;
        break;
      }

      double temp = cell.getValue ();
    }

    return this;
  }
}