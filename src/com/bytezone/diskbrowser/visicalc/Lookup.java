package com.bytezone.diskbrowser.visicalc;

class Lookup extends RangeFunction
{
  String sourceText;
  String rangeText;
  Expression source;

  public Lookup (Sheet parent, String text)
  {
    super (parent, text);

    int pos = text.indexOf (',');
    sourceText = text.substring (8, pos);
    rangeText = text.substring (pos + 1, text.length () - 1);
  }

  @Override
  public Value calculate ()
  {
    if (source == null)
      source = new Expression (parent, sourceText);

    source.calculate ();
    if (source.isError () || source.isNotAvailable ())
    {
      valueType = source.getValueType ();
      return this;
    }

    double sourceValue = source.getValue ();

    Address target = null;
    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell != null && cell.getValue () > sourceValue)
        break;
      target = address;
    }

    if (target != null)
    {
      if (range.isVertical ())
        value = parent.getCell (target.nextColumn ()).getValue ();
      else
        value = parent.getCell (target.nextRow ()).getValue ();
      valueType = ValueType.VALUE;
    }
    else
      System.out.println ("Target is null!");

    return this;
  }
}