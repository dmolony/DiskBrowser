package com.bytezone.diskbrowser.visicalc;

class Sum extends Function
{
  private final Range range;

  public Sum (Sheet parent, String text)
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
      if (cell == null)
        continue;

      if (cell.isError () || cell.isNaN ())
      {
        isError = true;
        break;
      }
      value += cell.getValue ();
    }
  }
}