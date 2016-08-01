package com.bytezone.diskbrowser.visicalc;

interface Value
{
  enum ValueType
  {
    VALUE, ERROR, NA, NAN
  }

  public ValueType getValueType ();

  public double getValue ();

  public String getText ();

  //  public boolean isValue ();
  //
  //  public boolean isError ();
  //
  //  public boolean isNotAvailable ();
  //
  //  public boolean isNotANumber ();
  public boolean is (ValueType valueType);

  public Value calculate ();
}