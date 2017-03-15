package com.bytezone.diskbrowser.visicalc;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Range implements Iterable<Address>
{
  private static final Pattern cellAddress = Pattern.compile ("[A-B]?[A-Z][0-9]{1,3}");
  private static final Pattern rangePattern =
      Pattern.compile ("([A-B]?[A-Z])([0-9]{1,3})\\.\\.\\.([A-B]?[A-Z])([0-9]{1,3})");
  private static final Pattern addressList = Pattern.compile ("\\(([^,]+(,[^,]+)*)\\)");

  private Address from, to;
  private final List<Address> range = new ArrayList<Address> ();
  private final Sheet parent;

  private boolean isHorizontal;

  public Range (Sheet parent, Cell cell, String rangeText)
  {
    this.parent = parent;

    Matcher m = rangePattern.matcher (rangeText);
    if (m.find ())
    {
      from = new Address (m.group (1), m.group (2));
      to = new Address (m.group (3), m.group (4));
      isHorizontal = from.rowMatches (to);
      populateRange ();
    }
    else
      throw new IllegalArgumentException (rangeText);
  }

  public Range (Sheet parent, Address from, Address to)
  {
    this.parent = parent;
    this.from = from;
    this.to = to;

    isHorizontal = from.rowMatches (to);
    populateRange ();
  }

  private void populateRange ()
  {
    range.add (from);
    parent.getCell (from);
    Address tempFrom = from;

    if (from.rowMatches (to))
      while (from.compareTo (to) < 0)
      {
        from = from.nextColumn ();
        range.add (from);
        parent.getCell (from);
      }
    else if (from.columnMatches (to))
      while (from.compareTo (to) < 0)
      {
        from = from.nextRow ();
        range.add (from);
        parent.getCell (from);
      }
    else
      throw new InvalidParameterException ();

    from = tempFrom;
  }

  boolean isHorizontal ()
  {
    return isHorizontal;
  }

  boolean isVertical ()
  {
    return !isHorizontal;
  }

  @Override
  public Iterator<Address> iterator ()
  {
    return range.iterator ();
  }

  public int size ()
  {
    return range.size ();
  }

  @Override
  public String toString ()
  {
    if (from == null || to == null)
    {
      StringBuilder text = new StringBuilder ();
      for (Address address : range)
        text.append (address.getText () + ",");
      if (text.length () > 0)
        text.deleteCharAt (text.length () - 1);
      return text.toString ();
    }
    return String.format ("      %s -> %s", from.getText (), to.getText ());
  }
}