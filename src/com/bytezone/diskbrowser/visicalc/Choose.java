package com.bytezone.diskbrowser.visicalc;

public class Choose extends Function
{
  private final Range range;
  private final String sourceText;
  private final String rangeText;
  private final Number source;

  Choose (Sheet parent, String text)
  {
    super (parent, text);

    int pos = text.indexOf (',');
    sourceText = text.substring (8, pos);
    source = new Number (sourceText);
    rangeText = text.substring (pos + 1, text.length () - 1);
    range = new Range (parent, rangeText);

    values.add (source);
  }
}