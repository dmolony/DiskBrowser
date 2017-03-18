package com.bytezone.diskbrowser.visicalc;

class Lookup extends Function
{
  private final String sourceText;
  private final String rangeText;

  private final Value source;
  private final Range range;

  public Lookup (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@LOOKUP(") : text;

    sourceText = Expression.getParameter (functionText);
    source = new Expression (parent, cell, sourceText).reduce ();
    values.add (source);

    rangeText = functionText.substring (sourceText.length () + 1);
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

  // @LOOKUP(B8,F3...F16)
  // @LOOKUP(.2*K8+K7,F3...F16)
}