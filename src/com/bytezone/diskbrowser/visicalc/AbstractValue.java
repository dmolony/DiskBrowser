package com.bytezone.diskbrowser.visicalc;

public abstract class AbstractValue implements Value
{
  protected final String typeText;

  public AbstractValue (String typeText)
  {
    this.typeText = typeText;
  }

  @Override
  public String getTypeText ()
  {
    return typeText;
  }
}