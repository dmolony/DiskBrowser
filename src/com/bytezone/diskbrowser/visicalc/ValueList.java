package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ValueList implements Iterable<Value>
{
  private final List<Value> values = new ArrayList<Value> ();
  private boolean rangeFound;

  public ValueList (Cell cell, String text)
  {
    String remainder = text;

    while (true)
    {
      String parameter = Expression.getParameter (remainder);

      if (Range.isRange (parameter))
      {
        rangeFound = true;
        for (Address address : new Range (cell, parameter))
          values.add (cell.getCell (address));
      }
      else
        values.add (new Expression (cell, parameter).reduce ());

      if (remainder.length () == parameter.length ())
        break;

      remainder = remainder.substring (parameter.length () + 1);
    }
  }

  public boolean hasRange ()
  {
    return rangeFound;
  }

  public Value get (int index)
  {
    return values.get (index);
  }

  public int size ()
  {
    return values.size ();
  }

  @Override
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }
}