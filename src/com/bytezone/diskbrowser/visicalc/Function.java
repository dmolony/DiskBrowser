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

abstract class Function implements Value
{
  private static final Pattern rangePattern = Pattern
      .compile ("\\(([A-B]?[A-Z])([0-9]{1,3})\\.\\.\\.([A-B]?[A-Z])([0-9]{1,3})\\)?");
  private static final Pattern addressList = Pattern.compile ("\\(([^,]+(,[^,]+)*)\\)");

  Sheet parent;
  String functionText;
  boolean hasValue;

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

    if (text.startsWith ("@IF("))
      return new If (parent, text);

    if (text.startsWith ("@OR("))
      return new Or (parent, text);

    if (text.startsWith ("@AND("))
      return new And (parent, text);

    if (text.startsWith ("@ISERROR("))
      return new IsError (parent, text);

    if (text.startsWith ("@ERROR("))
      return new Error (parent, text);

    System.out.printf ("Unknown function: %s%n", text);
    return new Error (parent, "@ERROR()");
  }

  Function (Sheet parent, String text)
  {
    this.parent = parent;

    // get function's parameter string
    int pos = text.indexOf ('(');
    this.functionText = text.substring (pos + 1, text.length () - 1);
  }

  @Override
  public boolean hasValue ()
  {
    return hasValue;
  }

  @Override
  public String getError ()
  {
    return hasValue ? "" : "Error";
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

  @Override
  public String toString ()
  {
    return String.format ("Function: %s", functionText);
  }
}