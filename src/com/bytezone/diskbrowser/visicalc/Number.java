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
    //    return String.format ("Number: %f", value);
    String line = "+-------------------------------------------------------------+";
    StringBuilder text = new StringBuilder ();
    text.append (line + "\n");
    text.append (String.format ("| %-10.10s: NUM : %-34.34s%-8.8s|%n",
        cell.getAddressText (), getFullText (), valueType));
    text.append (line);
    return text.toString ();
  }
}