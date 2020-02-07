package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.visicalc.Sheet;

// -----------------------------------------------------------------------------------//
public class VisicalcFile extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private static boolean debug;
  private Sheet sheet;

  // ---------------------------------------------------------------------------------//
  public VisicalcFile (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    if (sheet == null)
      sheet = new Sheet (buffer);

    StringBuilder text = new StringBuilder ();

    text.append ("Visicalc : " + name + "\n\n");
    text.append (sheet.getTextDisplay (debug));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public static void setDefaultDebug (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    debug = value;
  }

  // ---------------------------------------------------------------------------------//
  public static void setDebug (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    debug = value;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isVisicalcFile (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int firstByte = buffer[0] & 0xFF;
    if (firstByte != 0xBE && firstByte != 0xAF)
      return false;

    int last = buffer.length - 1;

    while (buffer[last] == 0)
      last--;

    if (buffer[last] != (byte) 0x8D)
      return false;

    return true;
  }
}