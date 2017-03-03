package com.bytezone.diskbrowser.visicalc;

class Lookup extends Function
{
  protected final Range range;
  String sourceText;
  String rangeText;
  Expression source;

  public Lookup (Sheet parent, String text)
  {
    super (parent, text);

    int pos = text.indexOf (',');
    sourceText = text.substring (8, pos);
    source = new Expression (parent, sourceText);
    values.add (source);
    rangeText = text.substring (pos + 1, text.length () - 1);
    range = new Range (parent, rangeText);
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
      if (cell != null && cell.getValue () > sourceValue)     // past the value
        break;
      target = address;
    }

    //    System.out.printf ("*****-----**** %s%n", target);
    if (target == null)
      valueType = ValueType.NA;
    else
    {
      Address adjacentAddress =
          range.isVertical () ? target.nextColumn () : target.nextRow ();

      if (parent.cellExists (adjacentAddress))
      {
        Cell adjacentCell = parent.getCell (adjacentAddress);
        if (adjacentCell != null)
          value = adjacentCell.getValue ();
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