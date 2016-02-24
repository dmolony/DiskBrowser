package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class VisicalcFile extends AbstractFile
{
  private VisicalcSpreadsheet sheet;

  public VisicalcFile (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getText ()
  {
    if (sheet == null)
      sheet = new VisicalcSpreadsheet (buffer);

    StringBuilder text = new StringBuilder ();

    text.append ("Visicalc : " + name + "\n");
    text.append ("Cells    : " + sheet.size () + "\n\n");
    text.append (sheet.getCells ());

    if (false)
    {
      text.append ("\n");
      for (String line : sheet.lines)
      {
        text.append ("\n");
        text.append (line);
      }
    }

    return text.toString ();
  }

  public static boolean isVisicalcFile (byte[] buffer)
  {
    if (false)
      System.out.println (HexFormatter.format (buffer));
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