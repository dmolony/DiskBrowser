package com.bytezone.diskbrowser.visicalc;

public abstract class ConstantFunction extends Function
{
  public ConstantFunction (Cell cell, String text)
  {
    super (cell, text);
  }

  @Override
  public String getType ()
  {
    return "ConstantFunction";
  }
}