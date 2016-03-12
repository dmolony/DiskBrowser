package com.bytezone.diskbrowser.visicalc;

interface Value
{
  public boolean hasValue ();

  public double getValue ();

  public String getError ();
}