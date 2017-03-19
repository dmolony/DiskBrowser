package com.bytezone.diskbrowser.visicalc;

class Lookup extends Function
{
  public Lookup (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@LOOKUP(") : text;

    // parameters are a Value, followed by a Range
    list = new ValueList (cell, functionText);

    for (Value v : list)
      values.add (v);
  }

  @Override
  public void calculate ()
  {
    Value source = list.get (0);

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
    Cell firstCell = (Cell) list.get (1);
    Cell lastCell = (Cell) list.get (list.size () - 1);
    boolean isVertical = firstCell.getAddress ().columnMatches (lastCell.getAddress ());

    for (int i = 1; i < list.size (); i++)        // skip first entry
    {
      Cell cell = (Cell) list.get (i);
      if (cell.getValue () > sourceValue)         // past the value
        break;
      target = cell.getAddress ();
    }

    if (target == null)
    {
      valueType = ValueType.NA;
      value = 0;
    }
    else
    {
      Address adjacentAddress = isVertical ? target.nextColumn () : target.nextRow ();

      //      Sheet parent = cell.getParent ();
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
}