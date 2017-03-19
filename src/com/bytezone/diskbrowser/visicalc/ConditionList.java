package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConditionList implements Iterable<Condition>
{
  private final List<Condition> conditions = new ArrayList<Condition> ();

  public ConditionList (Cell cell, String text)
  {
    //    Sheet parent = cell.getParent ();
    String remainder = text;

    while (true)
    {
      String parameter = Expression.getParameter (remainder);
      conditions.add (new Condition (cell, parameter));
      if (remainder.length () == parameter.length ())
        break;
      remainder = remainder.substring (parameter.length () + 1);
    }
  }

  public Condition get (int index)
  {
    return conditions.get (index);
  }

  public int size ()
  {
    return conditions.size ();
  }

  @Override
  public Iterator<Condition> iterator ()
  {
    return conditions.iterator ();
  }
}