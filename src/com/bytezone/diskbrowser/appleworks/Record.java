package com.bytezone.diskbrowser.appleworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// -----------------------------------------------------------------------------------//
class Record
// -----------------------------------------------------------------------------------//
{
  AppleworksADBFile parent;
  int length;
  List<String> items = new ArrayList<> ();
  Map<Integer, Double> calculatedItems = new HashMap<> ();      // move to TableReport

  // ---------------------------------------------------------------------------------//
  Record (AppleworksADBFile parent, byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;
    int count;

    while ((count = buffer[ptr++] & 0xFF) != 0xFF)
    {
      if (count < 0x80)         // size of category data
      {
        if (buffer[ptr] == (byte) 0xC0)           // date
        {
          String year = new String (buffer, ptr + 1, 2);
          int month = buffer[ptr + 3] - '@';
          String day = new String (buffer, ptr + 4, 2).trim ();
          if (day.length () == 1)
            day = "0" + day;
          items.add (String.format ("%2s/%02d/%2s", year, month, day));
        }
        else if (buffer[ptr] == (byte) 0xD4)      // time
        {
          int hour = buffer[ptr + 1] - '@';
          String minute = new String (buffer, ptr + 2, 2);
          items.add (String.format ("%02d:%s", hour, minute));
        }
        else
          items.add (new String (buffer, ptr, count));

        ptr += count;
      }
      else
        while (count-- > 0x80)
          items.add ("");
    }

    if (items.size () > parent.categories)
      System.out.println ("Too many items");

    while (items.size () < parent.categories)
      items.add ("");
  }

  // ---------------------------------------------------------------------------------//
  String getItem (int index)
  // ---------------------------------------------------------------------------------//
  {
    return items.get (index);
  }

  // ---------------------------------------------------------------------------------//
  double calculateItem (int pos, int name, String condition)
  // ---------------------------------------------------------------------------------//
  {
    try
    {
      // System.out.printf ("%nCalculating %d (%s): %s%n", pos, (char) name, condition);
      Pattern p = Pattern.compile ("([A-Za-z]{1,2})(([-+*/]([A-Za-z]{1,2}|[0-9]))*)");
      Matcher m = p.matcher (condition);
      if (m.matches ())
      {
        String init = m.group (1);
        String rest = m.group (2);

        double val = Double.parseDouble (valueOf (init.charAt (0)));

        Pattern p2 = Pattern.compile ("([-+*/])(([A-Za-z]{1,2})|([0-9]{1,6}))");
        Matcher m2 = p2.matcher (rest);

        while (m2.find ())
        {
          String operator = m2.group (1).trim ();

          double nextVal;
          if (m2.group (3) != null)
            nextVal = Double.parseDouble (valueOf (m2.group (3).charAt (0)));
          else
            nextVal = Double.parseDouble (m2.group (4));

          if (operator.equals ("+"))
            val += nextVal;
          else if (operator.equals ("-"))
            val -= nextVal;
          else if (operator.equals ("/"))
            val /= nextVal;
          else if (operator.equals ("*"))
            val *= nextVal;
          else
            System.out.println ("Unknown operator : " + operator);
        }

        calculatedItems.put (name, val);
        //        System.out.printf ("Putting %s : %f%n", (char) name, val);
        return val;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace ();
      return 0.0;
    }
    return 0.0;
  }

  // ---------------------------------------------------------------------------------//
  private String valueOf (int field)
  // ---------------------------------------------------------------------------------//
  {
    int itemNo = field - 'A';

    if (itemNo > 26)      // lowercase - calculated
    {
      if (calculatedItems.containsKey (field))
      {
        //        System.out.printf ("  Value of : [%s]", (char) field);
        //        System.out.println (" -> " + calculatedItems.get (field));
        return Double.toString (calculatedItems.get (field));
      }
      System.out.println ("Didn't find : " + field);
    }

    if (itemNo < items.size ())
    {
      //      System.out.printf ("  Value of : [%s]", (char) field);
      //      System.out.println (" -> " + items.get (itemNo));
      return items.get (itemNo);
    }

    System.out.printf (" -> can't find: %d out of %d%n", (itemNo + 1), items.size ());
    return "0.0";
  }

  // ---------------------------------------------------------------------------------//
  String getReportLine (String format)
  // ---------------------------------------------------------------------------------//
  {
    return String.format (format, (Object[]) items.toArray (new String[items.size ()]));
  }

  // ---------------------------------------------------------------------------------//
  String getReportLine ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    String format = String.format ("%%-%ds : %%s%%n", parent.maxCategoryName);

    int count = 0;
    for (String item : items)
    {
      if (count < parent.categoryNames.length)
        text.append (String.format (format, parent.categoryNames[count++], item));
      else
        text.append (item + "\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    String format = "%-" + parent.maxCategoryName + "s [%s]%n";

    int category = 0;
    for (String item : items)
      text.append (String.format (format, parent.categoryNames[category++], item));

    return text.toString ();
  }
}
