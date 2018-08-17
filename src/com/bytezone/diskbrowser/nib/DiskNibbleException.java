package com.bytezone.diskbrowser.nib;

public class DiskNibbleException extends Exception
{
  String message;

  public DiskNibbleException (String message)
  {
    this.message = message;
  }

  @Override
  public String toString ()
  {
    return message;
  }
}
