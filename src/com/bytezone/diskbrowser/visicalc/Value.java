package com.bytezone.diskbrowser.visicalc;

interface Value
{
  enum ValueType
  {
    VALUE, ERROR, NA, NAN
  }

  public double getValue ();

  public String getText ();

  public boolean isValueType (ValueType valueType);

  public ValueType getValueType ();

  public Value calculate ();
}