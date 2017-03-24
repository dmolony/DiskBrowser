package com.bytezone.diskbrowser.visicalc;

interface Value extends Iterable<Value>
{
  enum ValueType
  {
    NUMBER, BOOLEAN
  }

  enum ValueResult
  {
    ERROR, NA, VALID
  }

  public void calculate ();

  public boolean isValid ();                  // ValueResult.VALID

  public ValueType getValueType ();           // NUMBER, BOOLEAN

  public ValueResult getValueResult ();       // ERROR, NA, VALID

  public double getDouble ();                 // if ValueType == NUMBER

  public String getText ();                   // if ValueType == ERROR / NA / BOOLEAN

  public boolean getBoolean ();               // if ValueType == BOOLEAN

  public String getFullText ();               // original text

  public String getType ();                   // FUNCTION, CONDITION, EXPRESSION

  public int size ();
}