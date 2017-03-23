package com.bytezone.diskbrowser.visicalc;

class Lookup extends ValueListFunction
{
  public Lookup (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@LOOKUP(") : text;
    valueType = ValueType.NUMBER;
  }

  @Override
  public void calculate ()
  {
    Value source = list.get (0);                 // first Value is the value to look up
    valueResult = ValueResult.VALID;

    source.calculate ();

    if (!source.isValid ())
    {
      valueResult = source.getValueResult ();
      return;
    }

    if (list.size () <= 1)
    {
      valueResult = ValueResult.NA;
      return;
    }

    double sourceValue = source.getDouble ();
    Address target = null;

    // is the range horizontal or vertical?
    Cell firstCell = (Cell) list.get (1);
    Cell lastCell = (Cell) list.get (list.size () - 1);
    boolean isVertical = firstCell.getAddress ().columnMatches (lastCell.getAddress ());

    for (int i = 1; i < list.size (); i++)        // skip first entry
    {
      Cell cell = (Cell) list.get (i);
      if (cell.getDouble () > sourceValue)        // past the value
        break;
      target = cell.getAddress ();                // this could be the one
    }

    if (target == null)
    {
      valueResult = ValueResult.NA;
      return;
    }

    Address adjacentAddress = isVertical ? target.nextColumn () : target.nextRow ();

    if (cell.cellExists (adjacentAddress))
    {
      value = cell.getCell (adjacentAddress).getDouble ();
    }
    else
    {
      value = 0;
    }
  }
}