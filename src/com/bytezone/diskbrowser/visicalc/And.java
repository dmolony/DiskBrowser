package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;

class And extends Function
{
  private final List<Condition> conditions = new ArrayList<Condition> ();

  public And (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    String remainder = functionText;
    while (true)
    {
      String parameter = Expression.getParameter (remainder);
      System.out.printf ("cond: [%s]%n", parameter);
      conditions.add (new Condition (parent, cell, parameter));
      //      System.out.printf ("  [%s]%n", remainder);
      //      System.out.printf ("  [%s]%n", parameter);
      if (remainder.length () == parameter.length ())
        break;
      remainder = remainder.substring (parameter.length () + 1);
    }
  }

  @Override
  public void calculate ()
  {
    for (Condition condition : conditions)
    {
      condition.calculate ();
      if (condition.getValue () == 0)
      {
        value = 0;
        return;
      }
    }
    value = 1;
  }

  @Override
  public boolean isBoolean ()
  {
    return true;
  }

  @Override
  public String getText ()
  {
    return value == 0 ? "FALSE" : "TRUE";
  }
}