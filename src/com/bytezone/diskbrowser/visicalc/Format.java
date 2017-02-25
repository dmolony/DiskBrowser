package com.bytezone.diskbrowser.visicalc;

import java.text.DecimalFormat;

import com.bytezone.diskbrowser.visicalc.Value.ValueType;

public class Format
{
  private static final DecimalFormat nf = new DecimalFormat ("#####0.00");
  char cellFormat = ' ';

  String format (Value value, char defaultFormat, int colWidth)
  {
    char formatChar = cellFormat != ' ' ? cellFormat : defaultFormat;

    if (!value.isValueType (ValueType.VALUE))
    {
      //      char formatChar = format.cellFormat != ' ' ? format.cellFormat : defaultFormat;
      return justify (value.getText (), colWidth, formatChar);
    }

    if (formatChar == 'I')
    {
      String integerFormat = String.format ("%%%d.0f", colWidth);
      return String.format (integerFormat, value.getValue ());
    }
    else if (formatChar == '$')
    {
      String currencyFormat = String.format ("%%%d.%ds", colWidth, colWidth);
      return String.format (currencyFormat, nf.format (value.getValue ()));
    }
    else if (formatChar == '*')
    {
      String graphFormat = String.format ("%%-%d.%ds", colWidth, colWidth);
      // this is not finished
      return String.format (graphFormat, "********************");
    }
    else
    {
      // this could be improved
      String numberFormat = String.format ("%%%d.3f", colWidth + 4);
      String val = String.format (numberFormat, value.getValue ());
      while (val.endsWith ("0"))
        val = ' ' + val.substring (0, val.length () - 1);
      if (val.endsWith ("."))
        val = ' ' + val.substring (0, val.length () - 1);
      if (val.length () > colWidth)
        val = val.substring (val.length () - colWidth);
      return val;
    }
  }

  String justify (String text, int colWidth, char format)
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