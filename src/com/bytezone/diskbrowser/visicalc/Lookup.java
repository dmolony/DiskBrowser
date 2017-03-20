package com.bytezone.diskbrowser.visicalc;

class Lookup extends ValueListFunction
{
  public Lookup (Cell cell, String text)
  {
    super (cell, text);
    assert text.startsWith ("@LOOKUP(") : text;
  }

  @Override
  public void calculate ()
  {
    Value source = list.get (0);                 // first Value is the value to look up
    source.calculate ();

    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

    if (list.size () <= 1)
    {
      valueType = ValueType.NA;
      return;
    }

    double sourceValue = source.getValue ();
    Address target = null;

    // is the range horizontal or vertical?
    Cell firstCell = (Cell) list.get (1);
    Cell lastCell = (Cell) list.get (list.size () - 1);
    boolean isVertical = firstCell.getAddress ().columnMatches (lastCell.getAddress ());

    for (int i = 1; i < list.size (); i++)        // skip first entry
    {
      Cell cell = (Cell) list.get (i);
      if (cell.getValue () > sourceValue)         // past the value
        break;
      target = cell.getAddress ();                // this could be the one
    }

    if (target == null)
    {
      valueType = ValueType.NA;
      return;
    }

    Address adjacentAddress = isVertical ? target.nextColumn () : target.nextRow ();

    if (cell.cellExists (adjacentAddress))
    {
      value = cell.getCell (adjacentAddress).getValue ();
      valueType = ValueType.VALUE;
    }
    else
    {
      value = 0;
      valueType = ValueType.VALUE;
    }
  }
}