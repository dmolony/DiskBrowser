package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;

class Or extends Function
{
  private final List<Condition> conditions = new ArrayList<Condition> ();

  public Or (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    String list[] = text.split (",");
    for (String s : list)
      conditions.add (new Condition (parent, cell, s));
  }

  @Override
  public void calculate ()
  {
    for (Condition condition : conditions)
    {
      condition.calculate ();
      if (condition.getValue () == 1)
      {
        value = 1;
        return;
      }
    }
    value = 0;
  }
}