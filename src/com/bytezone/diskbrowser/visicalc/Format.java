package com.bytezone.diskbrowser.visicalc;

public class Format
{
  private static final String OVERFLOW = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
  private static final String HISTOGRAM = "***********************************";
  private static final String UNKNOWN = "???????????????????????????????????";

  private Format ()
  {

  }

  static String format (Value value, char formatChar, int colWidth)
  {
    double actualValue = value.getDouble ();
    if (actualValue == -0.0)
      actualValue = 0;

    String valueText = String.valueOf ((int) actualValue);
    if (valueText.startsWith ("0"))
      valueText = valueText.substring (1);
    int digits = valueText.length ();
    if (digits > colWidth)
      return OVERFLOW.substring (0, colWidth);

    switch (formatChar)
    {
      case 'L':
      case 'R':
      case 'G':
      case ' ':
        int precision = colWidth - (digits + 1);
        if (digits == 0)
          precision = colWidth - 1;
        if (precision < 0)
          precision = 0;
        String numberFormat = String.format ("%%%d.%df", colWidth, precision);
        String val = String.format (numberFormat, actualValue);
        //        System.out.printf ("%s %2d  %2d  %s  %15.8f  %s  : ", formatChar, colWidth,
        //            digits, numberFormat, actualValue, val);

        val = val.trim ();
        if (val.indexOf ('.') >= 0)
          while (val.endsWith ("0"))
            val = val.substring (0, val.length () - 1);
        if (val.endsWith ("."))
          val = val.substring (0, val.length () - 1);
        if (val.startsWith ("0."))
          val = val.substring (1);
        if (val.startsWith ("-0."))
          val = "-" + val.substring (2);

        if (val.length () > colWidth && val.indexOf ('.') >= 0)
          val = val.substring (0, colWidth);

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
          return OVERFLOW.substring (0, colWidth);

        return val;

      case 'I':
        String integerFormat = String.format ("%%%d.0f", colWidth);
        String result = String.format (integerFormat, actualValue);
        if (result.length () > colWidth)
          return OVERFLOW.substring (0, colWidth);
        return result;

      case '$':
        String currencyFormat = String.format ("%%%d.2f", colWidth + 3);
        result = String.format (currencyFormat, actualValue).trim ();
        String rightFormat = String.format ("%%%ds", colWidth);
        val = String.format (rightFormat, result);
        if (result.length () > colWidth)
          return OVERFLOW.substring (0, colWidth);
        return val;

      case '*':
        String graphFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
        return String.format (graphFormat, HISTOGRAM.substring (0, (int) actualValue));

      default:
        System.out.printf ("[%s]%n", formatChar);
        return UNKNOWN.substring (0, colWidth);
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