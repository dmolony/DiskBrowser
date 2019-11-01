package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

public class FontFile extends AbstractFile
{
  private static final int borderX = 3;
  private static final int borderY = 3;
  private static final int gapX = 3;
  private static final int gapY = 3;
  private static final int charsX = 8;
  private static final int charsY = 12;

  private static final int sizeX = 7;
  private static final int sizeY = 8;
  private static final int charBytes = 8;

  List<Character> characters = new ArrayList<Character> ();

  public FontFile (String name, byte[] buffer)
  {
    super (name, buffer);

    image = new BufferedImage (                         //
        dimension (charsX, borderX, sizeX, gapX),       //
        dimension (charsY, borderY, sizeY, gapY),       //
        BufferedImage.TYPE_BYTE_GRAY);

    Graphics2D g2d = image.createGraphics ();
    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));

    int ptr = 0;
    int x = borderX;
    int y = borderY;
    int count = 0;

    while (ptr < buffer.length)
    {
      Character c = new Character (buffer, ptr);
      characters.add (c);
      ptr += charBytes;

      g2d.drawImage (c.image, x, y, null);
      if (++count % charBytes == 0)
      {
        x = borderX;
        y += sizeY + gapY;
      }
      else
        x += sizeX + gapX;
    }

    g2d.dispose ();
  }

  private int dimension (int chars, int border, int size, int gap)
  {
    return border * 2 + chars * (size + gap) - gap;
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

  public static boolean isFont (byte[] buffer)
  {
    if (buffer.length % 8 != 0)
      return false;
    for (int i = 0; i < 8; i++)
      if (buffer[i] != 0 && buffer[i] != 0x7F)
        return false;
    return true;
  }

  class Character
  {
    String[] lines = new String[8];
    private final BufferedImage image;

    public Character (byte[] buffer, int ptr)
    {
      // draw the image
      image = new BufferedImage (sizeX, sizeY, BufferedImage.TYPE_BYTE_GRAY);
      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;

      for (int i = 0; i < charBytes; i++)
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