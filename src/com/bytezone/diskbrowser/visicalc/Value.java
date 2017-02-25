package com.bytezone.diskbrowser.visicalc;

interface Value
{
  enum ValueType
  {
    VALUE, ERROR, NA
  }

  public double getValue ();

  public String getText ();

  public boolean isValueType (ValueType valueType);

  public ValueType getValueType ();

  public Value calculate ();

  public String getTypeText ();
}