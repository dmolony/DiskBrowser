package com.bytezone.diskbrowser.visicalc;

// -----------------------------------------------------------------------------------//
class Lookup extends ValueListFunction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  Lookup (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);

    assert text.startsWith ("@LOOKUP(") : text;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void calculate ()
  // ---------------------------------------------------------------------------------//
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

    Address adjacentAddress = isVertical () ? target.nextColumn () : target.nextRow ();

    if (cell.cellExists (adjacentAddress))
      value = cell.getCell (adjacentAddress).getDouble ();
    else
      value = 0;
  }

  // is the range horizontal or vertical?
  // ---------------------------------------------------------------------------------//
  private boolean isVertical ()
  // ---------------------------------------------------------------------------------//
  {
    Cell firstCell = (Cell) list.get (1);
    Cell lastCell = (Cell) list.get (list.size () - 1);
    return firstCell.getAddress ().columnMatches (lastCell.getAddress ());
  }
}