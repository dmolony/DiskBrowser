package com.bytezone.diskbrowser.visicalc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// http://www.bricklin.com/history/refcard1.htm
// Functions:
// @AVERAGE
// @NPV
// @LOOKUP(v,range)
// @NA
// @ERROR
// @PI
// @ABS
// @INT
// @EXP
// @SQRT
// @LN
// @LOG10
// @SIN
// @ASIN
// @COS
// @ACOS
// @TAN
// @ATAN

// Unimplemented functions found so far:
// @IF
// @ISERROR
// @OR
// @AND

public abstract class Function
{
  private static final Pattern functionPattern = Pattern
      .compile ("\\(([A-B]?[A-Z])([0-9]{1,3})\\.\\.\\.([A-B]?[A-Z])([0-9]{1,3})\\)?");
  private static final Pattern addressList = Pattern.compile ("\\(([^,]+(,[^,]+)*)\\)");

  static Function getInstance (Sheet parent, String text)
  {
    if (text.startsWith ("@LOOKUP("))
      return new Lookup (parent, text);

    if (text.startsWith ("@COUNT("))
      return new Count (parent, text);

    if (text.startsWith ("@MIN("))
      return new Min (parent, text);

    if (text.startsWith ("@MAX("))
      return new Max (parent, text);

    if (text.startsWith ("@SUM("))
      return new Sum (parent, text);

    System.out.printf ("Unknown function: %s%n", text);
    return null;
  }

  abstract double getValue ();

  Range getRange (String text)
  {
    Range range = null;
    Matcher m = functionPattern.matcher (text);
    while (m.find ())
    {
      Address fromAddress = new Address (m.group (1), m.group (2));
      Address toAddress = new Address (m.group (3), m.group (4));
      range = new Range (fromAddress, toAddress);
    }

    if (range != null)
      return range;

    m = addressList.matcher (text);
    while (m.find ())
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

    if (range != null)
      return range;
    System.out.println ("null range : " + text);

    return range;
  }
}