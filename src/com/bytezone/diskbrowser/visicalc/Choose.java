package com.bytezone.diskbrowser.visicalc;

public class Choose extends Function
{
  private final Range range;
  private final String sourceText;
  private final String rangeText;
  private final Value source;

  Choose (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    int pos = functionText.indexOf (',');
    sourceText = functionText.substring (0, pos);
    source = new Expression (parent, cell, sourceText).reduce ();
    rangeText = functionText.substring (pos + 1);
    range = new Range (parent, cell, rangeText);

    values.add (source);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();
    int index = (int) source.getValue () - 1;
    if (index < 0 || index >= range.size ())
    {
      valueType = ValueType.NA;
      return;
    }

    Address address = range.get (index);
    if (address == null)
      valueType = ValueType.NA;
    else
    {
      Cell cell = parent.getCell (address);
      valueType = cell.getValueType ();
      value = cell.getValue ();
    }
  }
}