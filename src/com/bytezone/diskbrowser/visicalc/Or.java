package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;

class Or extends Function
{
  List<Condition> conditions = new ArrayList<Condition> ();

  public Or (Sheet parent, String text)
  {
    super (parent, text);
    String list[] = text.split (",");
    for (String s : list)
      conditions.add (new Condition (parent, s));
  }

  @Override
  public Value calculate ()
  {
    for (Condition condition : conditions)
    {
      condition.calculate ();
      if (condition.getValue () == 1)
      {
        value = 1;
        return this;
      }
    }
    value = 0;
    return this;
  }
}