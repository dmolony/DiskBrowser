package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ValueList implements Iterable<Value>
{
  protected List<Value> values = new ArrayList<Value> ();

  public ValueList (Cell cell, String text)
  {
    Sheet parent = cell.getParent ();
    String remainder = text;

    while (true)
    {
      String parameter = Expression.getParameter (remainder);

      if (Range.isRange (parameter))
        for (Address address : new Range (parent, cell, parameter))
          values.add (parent.getCell (address));
      else
        values.add (new Expression (parent, cell, parameter).reduce ());

      if (remainder.length () == parameter.length ())
        break;

      remainder = remainder.substring (parameter.length () + 1);
    }
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