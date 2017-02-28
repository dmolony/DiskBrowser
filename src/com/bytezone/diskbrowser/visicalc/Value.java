package com.bytezone.diskbrowser.visicalc;

interface Value
{
  enum ValueType
  {
    VALUE, ERROR, NA
  }

  public double getValue ();          // if ValueType == VALUE

  public String getText ();           // if ValueType != VALUE

  public boolean isValueType (ValueType valueType);

  public ValueType getValueType ();

  public void calculate ();

  public String getTypeText ();       // Number/Function/Expression etc
}