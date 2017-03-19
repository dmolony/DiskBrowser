package com.bytezone.diskbrowser.visicalc;

public abstract class ValueListFunction extends Function
{
  protected final ValueList list;
  protected final boolean isRange;

  public ValueListFunction (Cell cell, String text)
  {
    super (cell, text);

    list = new ValueList (cell, functionText);
    isRange = functionText.indexOf ("...") > 0;

    for (Value v : list)
      values.add (v);
  }

}