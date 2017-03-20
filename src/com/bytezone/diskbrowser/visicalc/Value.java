package com.bytezone.diskbrowser.visicalc;

interface Value
{
  enum ValueType
  {
    VALUE, ERROR, NA
  }

  public boolean isValueType (ValueType valueType);

  public ValueType getValueType ();

  public double getValue ();          // if ValueType == VALUE

  public String getText ();           // if ValueType != VALUE

  public void calculate ();

  public String getTypeText ();       // Number/Function/Expression etc

  public boolean isBoolean ();        // display TRUE/FALSE instead of 1/0
}