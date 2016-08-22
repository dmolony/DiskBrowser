package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;
import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class Wiz5Monsters extends AbstractFile
{
  List<Wiz4Image> images = new ArrayList<Wiz4Image> ();

  public Wiz5Monsters (String name, byte[] buffer)
  {
    super (name, buffer);

    for (int i = 0; i < 10; i++)
    {
      int ptr = buffer[i] * 512;
      byte[] data = new byte[512];
      System.arraycopy (buffer, ptr, data, 0, data.length);
      Wiz4Image image = new Wiz4Image ("Image " + i, data, 10, 6);  // no good
      images.add (image);
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    int ptr = 0;
    while (buffer[ptr] != 0)
    {
      int val1 = buffer[ptr] & 0xFF;
      int val2 = Utility.getWord (buffer, ptr * 2 + 256);
      text.append (String.format ("%3d  %02X  %04X :", ptr, val1, val2));
      String line = HexFormatter.getHexString (buffer, val1 * 512, 512);
      text.append (line);
      text.append ("\n");
      ptr++;
    }
    return text.toString ();
  }
}