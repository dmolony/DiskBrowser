package com.bytezone.diskbrowser.visicalc;

public class Lookup extends Function
{
  Range range;
  Cell source;
  Sheet parent;
  boolean hasValue;
  String sourceText;
  String rangeText;

  public Lookup (Sheet parent, String text)
  {
    this.parent = parent;

    int pos = text.indexOf (',');
    sourceText = text.substring (8, pos);
    rangeText = text.substring (pos + 1, text.length () - 1);
  }

  // need a mechanism to return NA and ERROR
  public boolean hasValue ()
  {
    return hasValue;
  }

  @Override
  public double getValue ()
  {
    // source could be a formula - @LOOKUP(.2*K8+K7,H3...H16)
    source = parent.getCell (new Address (sourceText));
    if (source == null)
    {
      System.out.println ("Null source:" + sourceText);
      return 0;
    }

    double sourceValue = source.getValue ();
    range = getRange (rangeText);

    Address target = null;
    for (Address address : range)
    {
      //      System.out.printf ("%s : %s%n", source, address);
      Cell cell = parent.getCell (address);
      if (cell != null && cell.getValue () > sourceValue)
        break;
      target = address;
    }

    if (target != null)
      if (range.isVertical ())
        return parent.getCell (target.nextColumn ()).getValue ();
      else
        return parent.getCell (target.nextRow ()).getValue ();

    return 0;
  }
}