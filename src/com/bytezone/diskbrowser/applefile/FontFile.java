package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

public class FontFile extends AbstractFile
{
  List<Character> characters = new ArrayList<Character> ();

  public FontFile (String name, byte[] buffer)
  {
    super (name, buffer);

    image = new BufferedImage (8 * (7 + 4), 12 * (8 + 4), BufferedImage.TYPE_BYTE_GRAY);
    Graphics2D g2d = image.createGraphics ();
    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));

    int ptr = 0;
    int x = 2;
    int y = 2;
    int count = 0;

    while (ptr < buffer.length)
    {
      Character c = new Character (buffer, ptr);
      ptr += 8;
      characters.add (c);

      g2d.drawImage (c.image, x, y, null);
      x += 7 + 4;
      if (++count % 8 == 0)
      {
        x = 2;
        y += 8 + 4;
      }
    }
    g2d.dispose ();
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");

    for (int i = 0; i < characters.size (); i += 8)
    {
      StringBuilder line = new StringBuilder ();
      for (int j = 0; j < 8; j++)
      {
        for (int k = 0; k < 8; k++)
        {
          line.append (characters.get (i + k).lines[j]);
          line.append ("    ");
        }
        line.append ("\n");
      }

      text.append (line.toString ());
      text.append ("\n");
    }

    return text.toString ();
  }

  class Character
  {
    String[] lines = new String[8];
    private final BufferedImage image;

    public Character (byte[] buffer, int ptr)
    {
      // draw the image
      image = new BufferedImage (7, 8, BufferedImage.TYPE_BYTE_GRAY);
      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;

      for (int i = 0; i < 8; i++)
      {
        int b = buffer[ptr + i] & 0xFF;
        String s = "0000000" + Integer.toString (b, 2);
        s = s.substring (s.length () - 7);
        s = s.replace ('0', ' ');
        s = s.replace ('1', 'O');
        s = new StringBuilder (s).reverse ().toString ();
        for (byte ch : s.getBytes ())
          dataBuffer.setElem (element++, ch == ' ' ? 0 : 255);
        lines[i] = s;
      }
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      for (String s : lines)
        text.append (s + "\n");
      text.deleteCharAt (text.length () - 1);

      return text.toString ();
    }
  }
}