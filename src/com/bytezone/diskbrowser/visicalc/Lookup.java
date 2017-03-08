package com.bytezone.diskbrowser.visicalc;

class Lookup extends Function
{
  private final String sourceText;
  private final String rangeText;
  private final Expression source;
  private final Range range;

  public Lookup (Sheet parent, String text)
  {
    super (parent, text);

    int pos = text.indexOf (',');

    sourceText = text.substring (8, pos);
    source = new Expression (parent, sourceText);

    rangeText = text.substring (pos + 1, text.length () - 1);
    range = new Range (parent, rangeText);

    values.add (source);
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
      if (cell.isValueType (ValueType.NA))
        continue;
      if (cell.getValue () > sourceValue)     // past the value
        break;
      target = address;
    }

    if (target == null)
      valueType = ValueType.NA;
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