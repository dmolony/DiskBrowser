package com.bytezone.diskbrowser.visicalc;

public class Choose extends Function
{
  private final String sourceText;
  private final String rangeText;

  private final Value source;
  private final Range range;

  Choose (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@CHOOSE(") : text;

    sourceText = Expression.getParameter (functionText);
    source = new Expression (parent, cell, sourceText).reduce ();
    values.add (source);

    rangeText = functionText.substring (sourceText.length () + 1);
    range = new Range (parent, cell, rangeText);
  }

  @Override
  public void calculate ()
  {
    source.calculate ();
    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

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