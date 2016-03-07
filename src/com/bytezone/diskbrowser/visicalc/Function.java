package com.bytezone.diskbrowser.visicalc;

public abstract class Function
{
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

    return null;
  }

  abstract double getValue ();
}