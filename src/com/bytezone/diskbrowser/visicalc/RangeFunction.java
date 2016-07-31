package com.bytezone.diskbrowser.visicalc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RangeFunction extends Function
{
  private static final Pattern rangePattern = Pattern
      .compile ("\\(([A-B]?[A-Z])([0-9]{1,3})\\.\\.\\.([A-B]?[A-Z])([0-9]{1,3})\\)?");
  private static final Pattern addressList = Pattern.compile ("\\(([^,]+(,[^,]+)*)\\)");
  protected final Range range;

  public RangeFunction (Sheet parent, String text)
  {
    super (parent, text);
    range = getRange (text);
  }

  protected Range getRange (String text)
  {
    Range range = null;
    Matcher m = rangePattern.matcher (text);
    if (m.find ())
    {
      Address fromAddress = new Address (m.group (1), m.group (2));
      Address toAddress = new Address (m.group (3), m.group (4));
      range = new Range (fromAddress, toAddress);
    }

    if (range != null)
      return range;

    m = addressList.matcher (text);
    if (m.find ())
    {
      String[] cells = m.group (1).split (",");
      range = new Range (cells);
    }

    if (range != null)
      return range;

    int pos = text.indexOf ("...");
    if (pos > 0)
    {
      String from = text.substring (0, pos);
      String to = text.substring (pos + 3);
      Address fromAddress = new Address (from);
      Address toAddress = new Address (to);
      range = new Range (fromAddress, toAddress);
    }

    if (range == null)
      System.out.printf ("null range [%s]%n", text);

    return range;
  }
}