package com.bytezone.diskbrowser.visicalc;

public class Format
{
  //  private static final DecimalFormat nf = new DecimalFormat ("#####0.00");

  private Format ()
  {

  }

  static String format (Value value, char formatChar, int colWidth)
  {
    double actualValue = value.getValue ();
    if (actualValue == -0.0)
      actualValue = 0;

    switch (formatChar)
    {
      case 'L':
      case 'R':
      case ' ':
        // this could be improved
        String numberFormat = String.format ("%%%d.7f", colWidth + 8);
        String val = String.format (numberFormat, actualValue);

        val = val.trim ();
        while (val.endsWith ("0"))
          val = val.substring (0, val.length () - 1);
        if (val.endsWith ("."))
          val = val.substring (0, val.length () - 1);
        if (val.startsWith ("0."))
          val = val.substring (1);

        if (val.length () > colWidth && val.indexOf ('.') >= 0)
        {
          val = val.substring (0, colWidth);
        }

        //      System.out.printf ("len:%d fmt: %s%n", val.length (), formatChar);
        if (formatChar == 'L')
        {
          String leftFormat = String.format ("%%-%ds", colWidth);
          val = String.format (leftFormat, val);
        }
        else
        {
          String rightFormat = String.format ("%%%ds", colWidth);
          val = String.format (rightFormat, val);
        }

        if (val.length () > colWidth)
          return ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>".substring (0, colWidth);

        return val;

      case 'I':
        String integerFormat = String.format ("%%%d.0f", colWidth);
        String result = String.format (integerFormat, actualValue);
        if (result.length () > colWidth)
          return ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>".substring (0, colWidth);
        return result;

      case '$':
        String currencyFormat = String.format ("%%%d.%df", colWidth + 3, 2);
        result = String.format (currencyFormat, actualValue).trim ();
        String rightFormat = String.format ("%%%ds", colWidth);
        val = String.format (rightFormat, result);
        //        System.out.println (result);
        //        System.out.println (val);
        if (result.length () > colWidth)
          return ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>".substring (0, colWidth);
        return val;

      case '*':
        String graphFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
        // this is not finished
        return String.format (graphFormat, "********************");

      default:
        System.out.printf ("[%s]%n", formatChar);
        return "??????????????????????".substring (0, colWidth);
    }
  }

  static String justify (String text, int colWidth, char format)
  {
    // right justify
    if (format == 'R' || format == '$' || format == 'I')
    {
      String labelFormat = String.format ("%%%d.%ds", colWidth, colWidth);
      return (String.format (labelFormat, text));
    }

    // left justify
    String labelFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
    return (String.format (labelFormat, text));
  }
}