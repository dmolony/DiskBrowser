package com.bytezone.diskbrowser.visicalc;

import java.util.Iterator;

abstract class Function extends AbstractValue implements Iterable<Value>
{
  static final String[] functionList =
      { "@ABS(", "@ACOS(", "@AND(", "@ASIN(", "@ATAN(", "@AVERAGE(", "@COUNT(",
        "@CHOOSE(", "@COS(", "@ERROR", "@EXP(", "@FALSE", "@IF(", "@INT(", "@ISERROR(",
        "@ISNA(", "@LOG10(", "@LOOKUP(", "@LN(", "@MIN(", "@MAX(", "@NA", "@NPV(", "@OR(",
        "@PI", "@SIN(", "@SUM(", "@SQRT(", "@TAN(", "@TRUE" };

  protected final Sheet parent;
  protected final Cell cell;
  protected String functionName;
  protected String functionText;
  protected String fullText;

  static Function getInstance (Sheet parent, Cell cell, String text)
  {
    if (text.charAt (0) != '@')
    {
      System.out.printf ("Unknown function: [%s]%n", text);
      return new Error (parent, cell, "@ERROR");
    }

    if (text.charAt (1) == 'A')
    {
      if (text.startsWith ("@ABS("))
        return new Abs (parent, cell, text);

      if (text.startsWith ("@ACOS("))
        return new Acos (parent, cell, text);

      if (text.startsWith ("@AND("))
        return new And (parent, cell, text);

      if (text.startsWith ("@ASIN("))
        return new Asin (parent, cell, text);

      if (text.startsWith ("@ATAN("))
        return new Atan (parent, cell, text);

      if (text.startsWith ("@AVERAGE("))
        return new Average (parent, cell, text);
    }
    else if (text.charAt (1) == 'C')
    {
      if (text.startsWith ("@COUNT("))
        return new Count (parent, cell, text);

      if (text.startsWith ("@CHOOSE("))
        return new Choose (parent, cell, text);

      if (text.startsWith ("@COS("))
        return new Cos (parent, cell, text);
    }
    else if (text.charAt (1) == 'E')
    {
      if (text.startsWith ("@ERROR"))
        return new Error (parent, cell, text);

      if (text.startsWith ("@EXP("))
        return new Exp (parent, cell, text);
    }
    else if (text.charAt (1) == 'F')
    {
      if (text.startsWith ("@FALSE"))
        return new False (parent, cell, text);
    }
    else if (text.charAt (1) == 'I')
    {
      if (text.startsWith ("@IF("))
        return new If (parent, cell, text);

      if (text.startsWith ("@INT("))
        return new Int (parent, cell, text);

      if (text.startsWith ("@ISERROR("))
        return new IsError (parent, cell, text);

      if (text.startsWith ("@ISNA("))
        return new IsNa (parent, cell, text);
    }
    else if (text.charAt (1) == 'L')
    {
      if (text.startsWith ("@LOG10("))
        return new Log10 (parent, cell, text);

      if (text.startsWith ("@LOOKUP("))
        return new Lookup (parent, cell, text);

      if (text.startsWith ("@LN("))
        return new Ln (parent, cell, text);
    }
    else if (text.charAt (1) == 'M')
    {
      if (text.startsWith ("@MIN("))
        return new Min (parent, cell, text);

      if (text.startsWith ("@MAX("))
        return new Max (parent, cell, text);
    }
    else if (text.charAt (1) == 'N')
    {
      if (text.equals ("@NA"))
        return new Na (parent, cell, text);

      if (text.startsWith ("@NPV("))
        return new Npv (parent, cell, text);
    }
    else if (text.charAt (1) == 'O')
    {
      if (text.startsWith ("@OR("))
        return new Or (parent, cell, text);
    }
    else if (text.charAt (1) == 'P')
    {
      if (text.startsWith ("@PI"))
        return new Pi (parent, cell, text);
    }
    else if (text.charAt (1) == 'S')
    {
      if (text.startsWith ("@SIN("))
        return new Sin (parent, cell, text);

      if (text.startsWith ("@SUM("))
        return new Sum (parent, cell, text);

      if (text.startsWith ("@SQRT("))
        return new Sqrt (parent, cell, text);
    }
    else if (text.charAt (1) == 'T')
    {
      if (text.startsWith ("@TAN("))
        return new Tan (parent, cell, text);

      if (text.startsWith ("@TRUE"))
        return new True (parent, cell, text);
    }

    System.out.printf ("Unknown function: [%s]%n", text);
    return new Error (parent, cell, "@ERROR");
  }

  Function (Sheet parent, Cell cell, String text)
  {
    super ("Function");
    this.parent = parent;
    this.cell = cell;
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