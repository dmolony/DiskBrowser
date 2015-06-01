package com.bytezone.diskbrowser.applefile;

public class DefaultAppleFile extends AbstractFile
{
  String text;

  public DefaultAppleFile (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  public DefaultAppleFile (String name, byte[] buffer, String text)
  {
    super (name, buffer);
    this.text = "Name : " + name + "\n\n" + text;
  }

  public void setText (String text)
  {
    this.text = text;
  }

  @Override
  public String getText ()
  {
    if (text != null)
      return text;
    if (buffer == null)
      return "Invalid file : " + name;
    return super.getText ();
  }
}