package com.bytezone.diskbrowser.visicalc;

class Count extends Function
{
  private final Range range;

  public Count (Sheet parent, String text)
  {
    super (parent, text);
    range = getRange (text);
  }

  @Override
  public void calculate ()
  {
    value = 0;
    for (Address address : range)
    {
      Cell cell = parent.getCell (address);
      if (cell == null || cell.isError () || cell.isNaN ())
      {
        valueType = ValueType.ERROR;
        break;
      }

      if (cell.getValue () != 0.0)
        value++;
    }
  }
}