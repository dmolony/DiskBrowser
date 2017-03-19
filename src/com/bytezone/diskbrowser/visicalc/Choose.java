package com.bytezone.diskbrowser.visicalc;

import com.bytezone.diskbrowser.visicalc.Cell.CellType;

public class Choose extends Function
{
  Choose (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@CHOOSE(") : text;

    list = new ValueList (cell, functionText);

    for (Value v : list)
      values.add (v);

    //    String sourceText = Expression.getParameter (functionText);
    //    source = cell.getExpressionValue (sourceText);
    //    values.add (source);
    //
    //    String rangeText = functionText.substring (sourceText.length () + 1);
    //    range = new Range (parent, cell, rangeText);
  }

  @Override
  public void calculate ()
  {
    Value source = list.get (0);

    source.calculate ();
    if (!source.isValueType (ValueType.VALUE))
    {
      valueType = source.getValueType ();
      return;
    }

    int index = (int) source.getValue ();
    if (index < 1 || index >= list.size ())
    {
      valueType = ValueType.NA;
      return;
    }

    Cell cell = (Cell) list.get (index);
    //    Address address = range.get (index);
    if (cell.isCellType (CellType.EMPTY))
      valueType = ValueType.NA;
    else
    {
      //      Cell cell = parent.getCell (address);
      valueType = cell.getValueType ();
      value = cell.getValue ();
    }
  }

  public void calculate2 ()
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