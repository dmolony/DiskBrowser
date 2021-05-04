package com.bytezone.diskbrowser.prodos.write;

public class FileAlreadyExistsException extends Exception
{
  public FileAlreadyExistsException (String message)
  {
    super (message);
  }
}
