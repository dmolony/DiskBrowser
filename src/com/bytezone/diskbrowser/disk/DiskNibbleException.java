package com.bytezone.diskbrowser.disk;

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
