package com.bytezone.diskbrowser.visicalc;

public class Choose extends Function
{
  Choose (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@CHOOSE(") : text;

    String sourceText = Expression.getParameter (functionText);
    source = cell.getExpressionValue (sourceText);
    values.add (source);

    String rangeText = functionText.substring (sourceText.length () + 1);
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