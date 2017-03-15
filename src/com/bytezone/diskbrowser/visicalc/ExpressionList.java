package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;
import java.util.regex.Pattern;

public class ExpressionList extends AbstractValue implements Iterable<Value>
{
  private static final Pattern cellAddress = Pattern.compile ("[A-B]?[A-Z][0-9]{1,3}");
  private static final Pattern addressList = Pattern.compile ("\\(([^,]+(,[^,]+)*)\\)");

  private final Sheet parent;

  public ExpressionList (Sheet parent, Cell cell, String text)
  {
    super ("expL");
    this.parent = parent;

    int ptr = 0;
    while (ptr < text.length ())
    {
      if (text.charAt (ptr) == '@')
      {
        String functionText = Expression.getBalancedText (text.substring (ptr));
        Value v = new Expression (parent, cell, functionText).reduce ();
        values.add (v);
        ptr += functionText.length ();
      }
      else
      {
        String item = getNextItem (text, ptr);
        int pos = item.indexOf ("...");
        if (pos > 0)         // range
        {
          String fromAddress = item.substring (0, pos);
          String toAddress = item.substring (pos + 3);

          Address from = new Address (fromAddress);
          Address to = new Address (toAddress);

          Range range = new Range (parent, from, to);

          for (Address address : range)
            values.add (parent.getCell (address));
        }
        else
        {
          Value v = new Expression (parent, cell, item).reduce ();
          values.add (v);
        }
        ptr += item.length ();
      }

      if (ptr < text.length () && text.charAt (ptr) == ',')
        ptr++;
      if (ptr < text.length () && text.charAt (ptr) == ')')
        ptr++;
    }
  }

  private String getNextItem (String text, int ptr)
  {
    int p = ptr;
    while (++p < text.length () && text.charAt (p) != ',')
      ;
    return text.substring (ptr, p);
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