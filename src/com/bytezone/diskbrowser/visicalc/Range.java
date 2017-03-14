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

  public Range (Sheet parent, String rangeText)
  {
    this.parent = parent;
    setRange (rangeText);

    createCells ();
  }

  public Range (Sheet parent, Address from, Address to)
  {
    this.from = from;
    this.to = to;
    this.parent = parent;

    addRange ();

    createCells ();
  }

  public Range (Sheet parent, String[] cells)
  {
    this.parent = parent;

    for (String s : cells)
      range.add (new Address (s));

    createCells ();
  }

  private void createCells ()
  {
    for (Address address : range)
      parent.getCell (address);
  }

  private void addRange ()
  {
    range.add (from);
    Address tempFrom = from;

    if (from.rowMatches (to))
      while (from.compareTo (to) < 0)
      {
        from = from.nextColumn ();
        range.add (from);
      }
    else if (from.columnMatches (to))
      while (from.compareTo (to) < 0)
      {
        from = from.nextRow ();
        range.add (from);
      }
    else
      throw new InvalidParameterException ();

    from = tempFrom;
  }

  boolean isHorizontal ()
  {
    Address first = range.get (0);
    Address last = range.get (range.size () - 1);
    return first.rowMatches (last);
  }

  boolean isVertical ()
  {
    Address first = range.get (0);
    Address last = range.get (range.size () - 1);
    return first.columnMatches (last);
  }

  @Override
  public Iterator<Address> iterator ()
  {
    return range.iterator ();
  }

  public int size ()
  {
    int total = 0;

    for (Address address : range)
      if (parent.getCell (address) != null)
        ++total;

    return total;
  }

  private void setRange (String text)
  {
    Matcher m = rangePattern.matcher (text);
    if (m.find ())
    {
      from = new Address (m.group (1), m.group (2));
      to = new Address (m.group (3), m.group (4));
      addRange ();
      return;
    }

    m = addressList.matcher (text);
    if (m.find ())
    {
      System.out.printf ("Address list: %s%n", text);
      String[] cells = m.group (1).split (",");
      for (String s : cells)
      {
        System.out.println (s);
        if (cellAddress.matcher (s).matches ())
          range.add (new Address (s));
        else
        {
          System.out.println ("Not a cell address: " + s);
        }
      }

      System.out.println ();
      return;
    }

    int pos = text.indexOf ("...");
    if (pos > 0)
    {
      String fromAddress = text.substring (0, pos);
      String toAddress = text.substring (pos + 3);
      from = new Address (fromAddress);
      to = new Address (toAddress);
      addRange ();
      return;
    }

    System.out.printf ("null range [%s]%n", text);
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