package com.bytezone.diskbrowser.visicalc;

public class Npv extends Function
{
  private final String valueText;
  private final String rangeText;

  private final Expression valueExp;
  private final Range range;

  Npv (Sheet parent, String text)
  {
    super (parent, text);

    int pos = text.indexOf (',');
    valueText = text.substring (8, pos);
    rangeText = text.substring (pos + 1, text.length () - 1);

    valueExp = new Expression (parent, valueText);
    range = getRange (rangeText);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    valueType = ValueType.VALUE;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null)
        continue;

      if (cell.isError () || cell.isNaN ())
      {
        valueType = ValueType.VALUE;
        return;
      }

      double temp = cell.getValue ();
    }
  }
}