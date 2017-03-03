package com.bytezone.diskbrowser.visicalc;

public class Choose extends Function
{
  protected final Range range;
  String sourceText;
  String rangeText;
  Number source;

  Choose (Sheet parent, String text)
  {
    super (parent, text);

    int pos = text.indexOf (',');
    sourceText = text.substring (8, pos);
    rangeText = text.substring (pos + 1, text.length () - 1);
    range = new Range (parent, rangeText);
    source = new Number (sourceText);
  }
}