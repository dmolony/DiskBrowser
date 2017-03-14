package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpressionList implements Iterable<Expression>
{
  private final Sheet parent;
  List<Expression> expressions = new ArrayList<Expression> ();

  public ExpressionList (Sheet parent, String rangeText)
  {
    this.parent = parent;
  }

  @Override
  public Iterator<Expression> iterator ()
  {
    return expressions.iterator ();
  }
}