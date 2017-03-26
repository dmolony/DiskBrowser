package com.bytezone.diskbrowser.visicalc;

public class IsNa extends BooleanFunction
{
  IsNa (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ISNA(") : text;
  }

  @Override
  public void calculate ()
  {
    source.calculate ();
    bool = source.getValueResult () == ValueResult.NA;
  }
}