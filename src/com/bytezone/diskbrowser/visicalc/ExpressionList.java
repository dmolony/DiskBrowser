package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;
import java.util.regex.Pattern;

public class ExpressionList extends AbstractValue implements Iterable<Value>
{
  private static final Pattern cellAddress = Pattern.compile ("[A-B]?[A-Z][0-9]{1,3}");
  private static final Pattern addressList = Pattern.compile ("\\(([^,]+(,[^,]+)*)\\)");

  private final Sheet parent;
  //  private final List<Expression> expressions = new ArrayList<Expression> ();

  public ExpressionList (Sheet parent, Cell cell, String text)
  {
    super ("expL");
    this.parent = parent;

    int pos = text.indexOf ("...");
    if (pos > 0)
    {
      String fromAddress = text.substring (0, pos);
      String toAddress = text.substring (pos + 3);

      Address from = new Address (fromAddress);
      Address to = new Address (toAddress);

      Range range = new Range (parent, from, to);
      for (Address address : range)
        values.add (parent.getCell (address));

      return;
    }

    String[] chunks = text.split (",");
    for (String s : chunks)
    {
      //      System.out.println (s);
      Value v = new Expression (parent, cell, s).reduce ();
      //      expressions.add (expression);
      values.add (v);
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