package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class Wiz4Monsters extends AbstractFile
{
  final List<Wiz4Image> images = new ArrayList<Wiz4Image> ();
  final List<Integer> blocks = new ArrayList<Integer> ();

  public Wiz4Monsters (String name, byte[] buffer)
  {
    super (name, buffer);

    int count = 0;
    for (int block = 0; block < 24; block++)
    {
      int ptr = block * 512;
      for (int pic = 0; pic < 2; pic++)
      {
        byte[] data = new byte[240];
        System.arraycopy (buffer, ptr + pic * 256, data, 0, data.length);
        Wiz4Image image = new Wiz4Image ("Image " + count++, data, 0, 5, 6);
        images.add (image);
        blocks.add (block);
      }
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    int count = 1;
    for (int block = 0; block < 24; block++)
    {
      int ptr = block * 512;
      for (int pic = 0; pic < 2; pic++)
      {
        text.append (String.format ("%3d : %s%n", count,
            HexFormatter.getHexString (buffer, ptr + pic * 256, 240)));
        count++;
      }
    }

    return text.toString ();
  }
}