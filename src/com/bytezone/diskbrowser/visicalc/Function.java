package com.bytezone.diskbrowser.visicalc;

abstract class Function extends AbstractValue
{
  static final String[] functionList =
      { "@ABS(", "@ACOS(", "@AND(", "@ASIN(", "@ATAN(", "@AVERAGE(", "@COUNT(",
        "@CHOOSE(", "@COS(", "@ERROR", "@EXP(", "@FALSE", "@IF(", "@INT(", "@ISERROR(",
        "@ISNA(", "@LOG10(", "@LOOKUP(", "@LN(", "@MIN(", "@MAX(", "@NA", "@NPV(", "@OR(",
        "@PI", "@SIN(", "@SUM(", "@SQRT(", "@TAN(", "@TRUE" };

  protected final String functionName;
  protected final String functionText;

  Function (Cell cell, String text)
  {
    super (cell, text);

    // get function's parameter string
    int pos = text.indexOf ('(');
    if (pos >= 0)
    {
      functionName = text.substring (0, pos);
      functionText = text.substring (pos + 1, text.length () - 1);
    }
    else
    {
      functionName = text;
      functionText = "";
    }
  }

  @Override
  public String toString ()
  {
    String line = "+-------------------------------------------------------------+";
    StringBuilder text = new StringBuilder ();
    text.append (line + "\n");
    text.append (String.format ("| %-10.10s: FN  : %-34.34s%-8.8s|%n",
        cell.getAddressText (), getFullText (), valueType));
    text.append (line);
    return text.toString ();
  }
}