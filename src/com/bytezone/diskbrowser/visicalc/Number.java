package com.bytezone.diskbrowser.visicalc;

class Number extends AbstractValue
{
  public Number (Cell cell, String text)
  {
    super (cell, text);

    try
    {
      valueType = ValueType.NUMBER;
      valueResult = ValueResult.VALID;
      value = Double.parseDouble (text);
    }
    catch (NumberFormatException e)
    {
      valueResult = ValueResult.ERROR;
      e.printStackTrace ();
    }
  }

  @Override
  public String getText ()
  {
    return value + "";
  }

  @Override
  public String getType ()
  {
    return "Constant";
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("%s%n", LINE));
    text.append (String.format (FMT5, cell.getAddressText (), "NUM", getFullText (),
        valueType, getValueText (this)));
    return text.toString ();
  }
}