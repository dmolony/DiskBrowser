package com.bytezone.diskbrowser.visicalc;

class Lookup extends Function
{
  Range range;
  String sourceText;
  String rangeText;
  Expression source;

  public Lookup (Sheet parent, String text)
  {
    super (parent, text);

    int pos = text.indexOf (',');
    sourceText = text.substring (8, pos);
    rangeText = text.substring (pos + 1, text.length () - 1);

    source = new Expression (parent, sourceText);
    range = getRange (rangeText);
  }

  @Override
  public Value calculate ()
  {
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
      if (range.isVertical ())
        value = parent.getCell (target.nextColumn ()).getValue ();
      else
        value = parent.getCell (target.nextRow ()).getValue ();

    return this;
  }
}