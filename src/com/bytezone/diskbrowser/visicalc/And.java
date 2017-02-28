package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;

class And extends Function
{
  List<Condition> conditions = new ArrayList<Condition> ();

  public And (Sheet parent, String text)
  {
    super (parent, text);
    String list[] = text.split (",");
    for (String s : list)
      conditions.add (new Condition (parent, s));
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
}