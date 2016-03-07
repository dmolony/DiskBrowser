package com.bytezone.diskbrowser.visicalc;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Range implements Iterable<Address>
{
  Address from, to;
  List<Address> range = new ArrayList<Address> ();

  public Range (Address from, Address to)
  {
    this.from = from;
    this.to = to;

    range.add (from);

    if (from.row == to.row)
      while (from.compareTo (to) < 0)
      {
        from = from.nextColumn ();
        range.add (from);
      }
    else if (from.column == to.column)
      while (from.compareTo (to) < 0)
      {
        from = from.nextRow ();
        range.add (from);
      }
    else
      throw new InvalidParameterException ();
  }

  public Range (String[] cells)
  {
    for (String s : cells)
      range.add (new Address (s));
  }

  boolean isHorizontal ()
  {
    Address first = range.get (0);
    Address last = range.get (range.size () - 1);
    return first.row == last.row;
  }

  boolean isVertical ()
  {
    Address first = range.get (0);
    Address last = range.get (range.size () - 1);
    return first.column == last.column;
  }

  @Override
  public String toString ()
  {
    if (from == null || to == null)
    {
      StringBuilder text = new StringBuilder ();
      for (Address address : range)
        text.append (address.text + ",");
      if (text.length () > 0)
        text.deleteCharAt (text.length () - 1);
      return text.toString ();
    }
    return String.format ("      %s -> %s", from.text, to.text);
  }

  @Override
  public Iterator<Address> iterator ()
  {
    return range.iterator ();
  }
}