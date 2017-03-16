package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ValueList implements Iterable<Value>
{
  private static final Pattern cellAddress = Pattern.compile ("[A-B]?[A-Z][0-9]{1,3}");
  private static final Pattern addressList = Pattern.compile ("\\(([^,]+(,[^,]+)*)\\)");

  private final Sheet parent;
  protected List<Value> values = new ArrayList<Value> ();

  public ValueList (Sheet parent, Cell cell, String text)
  {
    this.parent = parent;

    int ptr = 0;
    while (ptr < text.length ())
    {
      if (text.charAt (ptr) == '@')                         // function
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
        if (pos > 0)                                        // range
        {
          Address from = new Address (item.substring (0, pos));
          Address to = new Address (item.substring (pos + 3));
          Range range = new Range (parent, from, to);

          for (Address address : range)
            values.add (parent.getCell (address));
        }
        else                                                // cell/number/expression
        {
          Value v = new Expression (parent, cell, item).reduce ();
          values.add (v);
        }
        ptr += item.length ();
      }

      if (ptr < text.length () && text.charAt (ptr) == ',')
        ptr++;
      if (ptr < text.length () && text.charAt (ptr) == ')')
        break;
    }
  }

  // return substring of text from ptr up to the next comma
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