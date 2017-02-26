package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;

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

abstract class Function extends AbstractValue implements Iterable<Value>
{
  protected final Sheet parent;
  protected String functionName;
  protected String functionText;
  protected String fullText;

  static Function getInstance (Sheet parent, String text)
  {
    if (text.charAt (0) != '@')
    {
      System.out.printf ("Unknown function: [%s]%n", text);
      return new Error (parent, "@ERROR");
    }

    if (text.startsWith ("@ABS("))
      return new Abs (parent, text);

    if (text.startsWith ("@AND("))
      return new And (parent, text);

    if (text.startsWith ("@AVERAGE("))
      return new Average (parent, text);

    if (text.startsWith ("@COUNT("))
      return new Count (parent, text);

    if (text.startsWith ("@CHOOSE("))
      return new Choose (parent, text);

    if (text.startsWith ("@ERROR"))
      return new Error (parent, text);

    if (text.startsWith ("@IF("))
      return new If (parent, text);

    if (text.startsWith ("@INT("))
      return new Int (parent, text);

    if (text.startsWith ("@ISERROR("))
      return new IsError (parent, text);

    if (text.startsWith ("@ISNA("))
      return new IsNa (parent, text);

    if (text.startsWith ("@LOOKUP("))
      return new Lookup (parent, text);

    if (text.startsWith ("@MIN("))
      return new Min (parent, text);

    if (text.startsWith ("@MAX("))
      return new Max (parent, text);

    if (text.equals ("@NA"))
      return new Na (parent, text);

    if (text.startsWith ("@NPV("))
      return new Npv (parent, text);

    if (text.startsWith ("@OR("))
      return new Or (parent, text);

    if (text.startsWith ("@PI"))
      return new Pi (parent, text);

    if (text.startsWith ("@SUM("))
      return new Sum (parent, text);

    System.out.printf ("Unknown function: [%s]%n", text);
    return new Error (parent, "@ERROR");
  }

  Function (Sheet parent, String text)
  {
    super ("Function");
    this.parent = parent;
    fullText = text;

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
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }

  @Override
  public String toString ()
  {
    return String.format ("Function: %s %s", functionName, functionText);
  }
}