package com.bytezone.diskbrowser.visicalc;

public interface Value
{
  public boolean hasValue ();

  public double getValue ();

  public String getError ();
}