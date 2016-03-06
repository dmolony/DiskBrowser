package com.bytezone.diskbrowser.visicalc;

public class Lookup
{
  Range range;
  VisicalcCell source;
  VisicalcSpreadsheet parent;
  boolean hasValue;

  public Lookup (VisicalcSpreadsheet parent, String text)
  {
    this.parent = parent;

    int pos = text.indexOf (',');
    String sourceText = text.substring (8, pos);
    String rangeText = text.substring (pos + 1, text.length () - 1);

    source = parent.getCell (new Address (sourceText));
    range = parent.getRange (rangeText);
  }

  // need a mechanism to return NA and ERROR
  public boolean hasValue ()
  {
    return hasValue;
  }

  public double getValue ()
  {
    Address target = null;
    for (Address address : range)
    {
      if (parent.getCell (address).getValue () > source.getValue ())
        break;
      target = address;
    }

    if (target != null)
      return parent.getCell (target.nextColumn ()).getValue ();

    return 0;
  }
}