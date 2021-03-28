package com.bytezone.diskbrowser.applefile;

// -----------------------------------------------------------------------------------//
public class PascalInfo extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private static final byte CR = 0x0D;

  // ---------------------------------------------------------------------------------//
  public PascalInfo (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (getHeader ());

    for (int i = 0; i < buffer.length; i++)
      text.append (buffer[i] == CR ? "\n" : (char) buffer[i]);

    return text.toString ();
  }
}