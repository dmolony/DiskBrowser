package com.bytezone.diskbrowser.visicalc;

class IsError extends BooleanFunction
{
  public IsError (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@ISERROR(") : text;
  }

  @Override
  public void calculate ()
  {
    source.calculate ();
    bool = source.getValueResult () == ValueResult.ERROR;
  }
}