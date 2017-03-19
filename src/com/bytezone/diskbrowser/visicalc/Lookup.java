package com.bytezone.diskbrowser.visicalc;

class Lookup extends Function
{
  public Lookup (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@LOOKUP(") : text;

    String sourceText = Expression.getParameter (functionText);
    source = cell.getExpressionValue (sourceText);
    values.add (source);

    String rangeText = functionText.substring (sourceText.length () + 1);
    range = new Range (parent, cell, rangeText);
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

    if (range.size () == 0)
    {
      valueType = ValueType.NA;
      return;
    }

    double sourceValue = source.getValue ();
    Address target = null;

    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell.getValue () > sourceValue)     // past the value
        break;
      target = address;
    }

    if (target == null)
    {
      valueType = ValueType.NA;
      value = 0;
    }
    else
    {
      Address adjacentAddress =
          range.isVertical () ? target.nextColumn () : target.nextRow ();

      if (parent.cellExists (adjacentAddress))
      {
        value = parent.getCell (adjacentAddress).getValue ();
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