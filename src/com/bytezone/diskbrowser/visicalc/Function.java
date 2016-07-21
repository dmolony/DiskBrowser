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

  protected final Sheet parent;
  protected String functionName;
  protected String functionText;

  protected ValueType valueType;
  protected double value;

  static Function getInstance (Sheet parent, String text)
  {
    if (text.charAt (0) != '@')
    {
      System.out.printf ("Unknown function: [%s]%n", text);
      return new Error (parent, "@ERROR");
    }

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

    if (text.startsWith ("@NPV("))
      return new Npv (parent, text);

    if (text.startsWith ("@ABS("))
      return new Abs (parent, text);

    if (text.startsWith ("@INT("))
      return new Int (parent, text);

    if (text.startsWith ("@ISERROR("))
      return new IsError (parent, text);

    if (text.startsWith ("@ISNA("))
      return new IsNa (parent, text);

    if (text.startsWith ("@PI"))
      return new Pi (parent, text);

    if (text.startsWith ("@ERROR"))
      return new Error (parent, text);

    if (text.equals ("@NA"))
      return new Na (parent, text);

    System.out.printf ("Unknown function: [%s]%n", text);
    return new Error (parent, "@ERROR");
  }

  Function (Sheet parent, String text)
  {
    this.parent = parent;

    // get function's parameter string
    int pos = text.indexOf ('(');
    if (pos >= 0)
    {
      functionName = text.substring (0, pos);
      functionText = text.substring (pos + 1, text.length () - 1);
    }
    else
    {
      functionName = "";
      functionText = "";
    }
  }

  @Override
  public ValueType getValueType ()
  {
    return valueType;
  }

  @Override
  public boolean isValue ()
  {
    return valueType == ValueType.VALUE;
  }

  @Override
  public boolean isError ()
  {
    return valueType == ValueType.ERROR;
  }

  @Override
  public boolean isNotAvailable ()
  {
    return valueType == ValueType.NA;
  }

  @Override
  public boolean isNotANumber ()
  {
    return valueType == ValueType.NAN;
  }

  @Override
  public double getValue ()
  {
    //    System.out.printf ("Getting value of : %s %s%n", functionName, functionText);
    assert valueType == ValueType.VALUE : "Function ValueType = " + valueType;
    return value;
  }

  @Override
  public String getText ()
  {
    return isNotAvailable () ? "NA" : isError () ? "Error" : isNotANumber () ? "NaN" : "";
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
    return String.format ("Function: %s %s", functionName, functionText);
  }
}