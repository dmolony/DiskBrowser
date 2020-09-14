package com.bytezone.diskbrowser.applefile;

// -----------------------------------------------------------------------------------//
public class ErrorMessageFile extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  String text;

  // ---------------------------------------------------------------------------------//
  public ErrorMessageFile (String name, byte[] buffer, Exception e)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    StringBuilder text = new StringBuilder ();
    text.append ("Oops! : " + e.toString () + "\n\n");

    for (StackTraceElement ste : e.getStackTrace ())
      text.append (ste + "\n");

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    this.text = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return text;
  }
}